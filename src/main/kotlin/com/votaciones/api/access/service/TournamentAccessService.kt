package com.votaciones.api.access.service

import com.votaciones.api.access.domain.TournamentAccessMode
import com.votaciones.api.access.domain.TournamentJoinSessionEntity
import com.votaciones.api.access.dto.JoinByDisplayNameRequest
import com.votaciones.api.access.dto.JoinByEmailPasswordRequest
import com.votaciones.api.access.dto.JoinByPinRequest
import com.votaciones.api.access.dto.JoinByQrRequest
import com.votaciones.api.access.dto.SessionSummaryResponse
import com.votaciones.api.access.dto.TournamentAccessResponse
import com.votaciones.api.access.dto.TournamentJoinSessionResponse
import com.votaciones.api.access.dto.UpdateTournamentAccessRequest
import com.votaciones.api.access.repository.TournamentJoinSessionRepository
import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import com.votaciones.api.audit.service.AuditService
import com.votaciones.api.common.exception.BadRequestException
import com.votaciones.api.common.exception.NotFoundException
import com.votaciones.api.common.exception.UnauthorizedOperationException
import com.votaciones.api.config.AppProperties
import com.votaciones.api.security.AuthorizationService
import com.votaciones.api.security.TokenHashService
import com.votaciones.api.tournament.domain.TournamentEntity
import com.votaciones.api.tournament.domain.TournamentStatus
import com.votaciones.api.tournament.service.TournamentService
import com.votaciones.api.user.domain.RoleName
import com.votaciones.api.user.domain.UserEntity
import com.votaciones.api.user.repository.RoleRepository
import com.votaciones.api.user.repository.UserRepository
import com.votaciones.api.tournament.repository.TournamentRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TournamentAccessService(
    private val tournamentService: TournamentService,
    private val joinSessionRepository: TournamentJoinSessionRepository,
    private val authorizationService: AuthorizationService,
    private val tournamentSessionTokenService: TournamentSessionTokenService,
    private val tokenHashService: TokenHashService,
    private val auditService: AuditService,
    private val appProperties: AppProperties,
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional(readOnly = true)
    fun getTournamentAccess(tournamentId: UUID): TournamentAccessResponse {
        val tournament = tournamentService.getEntity(tournamentId)
        authorizationService.assertCanManageTournament(tournament)
        return toAccessResponse(tournament)
    }

    @Transactional
    fun updateTournamentAccess(tournamentId: UUID, request: UpdateTournamentAccessRequest): TournamentAccessResponse {
        val tournament = tournamentService.getEntity(tournamentId)
        authorizationService.assertCanManageTournament(tournament)
        tournament.accessMode = request.mode
        auditService.log(
            action = AuditAction.TOURNAMENT_ACCESS_UPDATED,
            entityType = AuditEntityType.TOURNAMENT,
            entityId = tournament.id,
            tournamentId = tournament.id,
            details = mapOf("accessMode" to request.mode.name),
        )
        return toAccessResponse(tournament)
    }

    @Transactional
    fun regeneratePin(tournamentId: UUID): TournamentAccessResponse {
        val tournament = tournamentService.getEntity(tournamentId)
        authorizationService.assertCanManageTournament(tournament)
        tournament.joinPin = tournamentSessionTokenService.randomPin()
        tournament.qrToken = tournamentSessionTokenService.randomToken()
        auditService.log(
            action = AuditAction.TOURNAMENT_PIN_REGENERATED,
            entityType = AuditEntityType.TOURNAMENT,
            entityId = tournament.id,
            tournamentId = tournament.id,
        )
        return toAccessResponse(tournament)
    }

    @Transactional(readOnly = true)
    fun resolveTournamentByPin(request: JoinByPinRequest): TournamentAccessResponse {
        val tournament = findByPin(request.pin)
        assertJoinable(tournament)
        return toAccessResponse(tournament)
    }

    @Transactional(readOnly = true)
    fun resolveTournamentByQr(request: JoinByQrRequest): TournamentAccessResponse {
        val tournament = findByQrToken(request.qrToken)
        assertJoinable(tournament)
        return toAccessResponse(tournament)
    }

    @Transactional
    fun joinByDisplayName(request: JoinByDisplayNameRequest): TournamentJoinSessionResponse {
        val tournament = resolveTournament(request.pin, request.qrToken)
        assertJoinable(tournament)
        if (tournament.accessMode == TournamentAccessMode.EMAIL_PASSWORD) {
            throw BadRequestException("This tournament requires email and password")
        }
        if (tournament.accessMode == TournamentAccessMode.ANONYMOUS && request.displayName.isBlank()) {
            throw BadRequestException("Display name is required for this request")
        }

        val session = createSession(
            tournament = tournament,
            user = null,
            displayName = request.displayName.trim(),
        )
        return toJoinResponse(session.first, session.second)
    }

    @Transactional
    fun joinAnonymously(request: JoinByQrRequest): TournamentJoinSessionResponse {
        val tournament = findByQrToken(request.qrToken)
        assertJoinable(tournament)
        if (tournament.accessMode != TournamentAccessMode.ANONYMOUS) {
            throw BadRequestException("This tournament does not allow anonymous access")
        }
        val session = createSession(tournament = tournament, user = null, displayName = null)
        return toJoinResponse(session.first, session.second)
    }

    @Transactional
    fun joinByEmailPassword(request: JoinByEmailPasswordRequest): TournamentJoinSessionResponse {
        val tournament = resolveTournament(request.pin, request.qrToken)
        assertJoinable(tournament)
        if (tournament.accessMode != TournamentAccessMode.EMAIL_PASSWORD) {
            throw BadRequestException("This tournament does not require email/password access")
        }

        val user = userRepository.findByEmail(request.email.trim().lowercase()).orElseGet {
            val voterRole = roleRepository.findByName(RoleName.VOTER)
                ?: throw NotFoundException("Default VOTER role not found")
            userRepository.save(
                UserEntity(
                    username = nextAvailableUsername(request.email.trim().substringBefore("@")),
                    email = request.email.trim().lowercase(),
                    passwordHash = passwordEncoder.encode(request.password) ?: throw UnauthorizedOperationException("Could not encode password"),
                    firstName = request.firstName?.trim().orEmpty().ifBlank { "Voter" },
                    lastName = request.lastName?.trim().orEmpty().ifBlank { "Guest" },
                    roles = mutableSetOf(voterRole),
                ),
            )
        }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedOperationException("Invalid email or password")
        }

        val session = createSession(
            tournament = tournament,
            user = user,
            displayName = listOf(user.firstName, user.lastName).joinToString(" ").trim(),
        )
        return toJoinResponse(session.first, session.second)
    }

    @Transactional(readOnly = true)
    fun getSessionByToken(sessionToken: String): TournamentJoinSessionEntity {
        val tokenHash = tokenHashService.hash(sessionToken)
        val session = joinSessionRepository.findBySessionTokenHashAndActiveTrue(tokenHash)
            ?: throw UnauthorizedOperationException("Invalid tournament session")
        if (session.expiresAt?.isBefore(Instant.now()) == true) {
            throw UnauthorizedOperationException("Tournament session expired")
        }
        return session
    }

    @Transactional
    fun touchSession(sessionToken: String): SessionSummaryResponse {
        val session = getSessionByToken(sessionToken)
        session.lastSeenAt = Instant.now()
        return SessionSummaryResponse(
            sessionId = session.id,
            tournamentId = session.tournament.id,
            displayName = session.displayName,
            userId = session.user?.id,
            joinedAt = session.joinedAt,
            lastSeenAt = session.lastSeenAt,
        )
    }

    private fun createSession(
        tournament: TournamentEntity,
        user: UserEntity?,
        displayName: String?,
    ): Pair<TournamentJoinSessionEntity, String> {
        val rawToken = tournamentSessionTokenService.randomToken()
        val session = joinSessionRepository.save(
            TournamentJoinSessionEntity(
                tournament = tournament,
                user = user,
                displayName = displayName,
                sessionTokenHash = tokenHashService.hash(rawToken),
                expiresAt = tournament.endAt,
            ),
        )

        auditService.log(
            action = AuditAction.JOIN_SESSION_CREATED,
            entityType = AuditEntityType.AUTH,
            entityId = session.id,
            tournamentId = tournament.id,
            details = mapOf("joinSession" to true, "mode" to tournament.accessMode.name),
            user = user,
        )

        return session to rawToken
    }

    private fun assertJoinable(tournament: TournamentEntity) {
        if (tournament.status !in setOf(TournamentStatus.PUBLISHED, TournamentStatus.ACTIVE, TournamentStatus.PAUSED)) {
            throw BadRequestException("Tournament is not available for joining")
        }
    }

    private fun findByPin(pin: String): TournamentEntity = tournamentRepository.findByJoinPin(pin.trim())
        ?: throw NotFoundException("Tournament with pin ${pin.trim()} not found")

    private fun findByQrToken(qrToken: String): TournamentEntity = tournamentRepository.findByQrToken(qrToken.trim())
        ?: throw NotFoundException("Tournament not found for qr token")

    private fun resolveTournament(pin: String?, qrToken: String?): TournamentEntity {
        return when {
            !pin.isNullOrBlank() -> findByPin(pin)
            !qrToken.isNullOrBlank() -> findByQrToken(qrToken)
            else -> throw BadRequestException("Provide pin or qrToken")
        }
    }

    private fun toAccessResponse(tournament: TournamentEntity): TournamentAccessResponse = TournamentAccessResponse(
        tournamentId = tournament.id,
        mode = tournament.accessMode,
        joinPin = tournament.joinPin,
        qrToken = tournament.qrToken,
        joinUrl = "${appProperties.baseUrl.trimEnd('/')}/join/${tournament.qrToken}",
    )

    private fun toJoinResponse(session: TournamentJoinSessionEntity, rawToken: String): TournamentJoinSessionResponse =
        TournamentJoinSessionResponse(
            tournamentId = session.tournament.id,
            tournamentTitle = session.tournament.title,
            mode = session.tournament.accessMode,
            sessionToken = rawToken,
            displayName = session.displayName,
            userId = session.user?.id,
            joinedAt = session.joinedAt,
            expiresAt = session.expiresAt,
        )

    private fun nextAvailableUsername(baseUsername: String): String {
        val normalizedBase = baseUsername.lowercase().replace(Regex("[^a-z0-9._-]"), "").ifBlank { "voter" }
        if (!userRepository.existsByUsername(normalizedBase)) {
            return normalizedBase
        }

        var attempt = 1
        while (true) {
            val candidate = "$normalizedBase$attempt"
            if (!userRepository.existsByUsername(candidate)) {
                return candidate
            }
            attempt++
        }
    }
}
