package com.votaciones.api.auth.service

import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import com.votaciones.api.audit.service.AuditService
import com.votaciones.api.auth.domain.RefreshTokenEntity
import com.votaciones.api.auth.dto.AuthResponse
import com.votaciones.api.auth.dto.LoginRequest
import com.votaciones.api.auth.dto.RefreshTokenRequest
import com.votaciones.api.auth.dto.RegisterRequest
import com.votaciones.api.auth.dto.TokenResponse
import com.votaciones.api.auth.repository.RefreshTokenRepository
import com.votaciones.api.common.exception.ConflictException
import com.votaciones.api.common.exception.NotFoundException
import com.votaciones.api.common.exception.UnauthorizedOperationException
import com.votaciones.api.security.JwtProperties
import com.votaciones.api.security.JwtService
import com.votaciones.api.security.TokenHashService
import com.votaciones.api.user.domain.RoleName
import com.votaciones.api.user.domain.UserEntity
import com.votaciones.api.user.mapper.UserMapper
import com.votaciones.api.user.repository.RoleRepository
import com.votaciones.api.user.repository.UserRepository
import com.votaciones.api.user.service.UserService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    private val tokenHashService: TokenHashService,
    private val userService: UserService,
    private val auditService: AuditService,
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw ConflictException("Username is already in use")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email is already in use")
        }

        val organizerRole = roleRepository.findByName(RoleName.ORGANIZER)
            ?: throw NotFoundException("Default ORGANIZER role not found")

        val user = userRepository.save(
            UserEntity(
                username = request.username.trim(),
                email = request.email.trim().lowercase(),
                passwordHash = passwordEncoder.encode(request.password) ?: throw UnauthorizedOperationException("Could not encode password"),
                firstName = request.firstName.trim(),
                lastName = request.lastName.trim(),
                enabled = true,
                roles = mutableSetOf(organizerRole),
            ),
        )

        auditService.log(
            action = AuditAction.REGISTER,
            entityType = AuditEntityType.USER,
            entityId = user.id,
            details = mapOf("username" to user.username, "email" to user.email),
            user = user,
        )

        return buildAuthResponse(user)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val user = findUserByIdentifier(request.usernameOrEmail)
        if (!user.enabled) {
            throw UnauthorizedOperationException("Account is disabled")
        }
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedOperationException("Invalid credentials")
        }

        auditService.log(
            action = AuditAction.LOGIN,
            entityType = AuditEntityType.AUTH,
            entityId = user.id,
            details = mapOf("username" to user.username),
            user = user,
        )

        return buildAuthResponse(user)
    }

    @Transactional
    fun refresh(request: RefreshTokenRequest): AuthResponse {
        val userId = jwtService.validateRefreshToken(request.refreshToken)
        val refreshTokenHash = tokenHashService.hash(request.refreshToken)
        val persistedToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(refreshTokenHash)
            ?: throw UnauthorizedOperationException("Refresh token is invalid or already rotated")

        if (persistedToken.user.id != userId || persistedToken.expiresAt.isBefore(Instant.now())) {
            throw UnauthorizedOperationException("Refresh token has expired")
        }

        persistedToken.revoked = true
        persistedToken.revokedAt = Instant.now()
        refreshTokenRepository.save(persistedToken)

        val user = persistedToken.user
        if (!user.enabled) {
            throw UnauthorizedOperationException("Account is disabled")
        }

        return buildAuthResponse(user)
    }

    @Transactional(readOnly = true)
    fun me() = userService.getCurrentUser()

    private fun buildAuthResponse(user: UserEntity): AuthResponse {
        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)
        persistRefreshToken(user, refreshToken)

        return AuthResponse(
            tokens = TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresInSeconds = jwtProperties.accessTokenTtl.seconds,
            ),
            user = UserMapper.toResponse(user),
        )
    }

    private fun persistRefreshToken(user: UserEntity, rawToken: String) {
        refreshTokenRepository.save(
            RefreshTokenEntity(
                user = user,
                tokenHash = tokenHashService.hash(rawToken),
                expiresAt = Instant.now().plus(jwtProperties.refreshTokenTtl),
            ),
        )
    }

    private fun findUserByIdentifier(identifier: String): UserEntity {
        val normalized = identifier.trim()
        val byEmail = if (normalized.contains("@")) userRepository.findByEmail(normalized.lowercase()) else null
        return byEmail?.orElse(null)
            ?: userRepository.findByUsername(normalized).orElseThrow {
                UnauthorizedOperationException("Invalid credentials")
            }
    }
}
