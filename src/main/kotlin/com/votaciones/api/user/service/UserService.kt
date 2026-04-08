package com.votaciones.api.user.service

import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import com.votaciones.api.audit.service.AuditService
import com.votaciones.api.common.dto.PageResponse
import com.votaciones.api.common.exception.NotFoundException
import com.votaciones.api.common.util.PageMapper
import com.votaciones.api.security.SecurityUtils
import com.votaciones.api.user.domain.RoleName
import com.votaciones.api.user.domain.UserEntity
import com.votaciones.api.user.dto.UpdateUserRolesRequest
import com.votaciones.api.user.dto.UpdateUserStatusRequest
import com.votaciones.api.user.dto.UserResponse
import com.votaciones.api.user.mapper.UserMapper
import com.votaciones.api.user.repository.RoleRepository
import com.votaciones.api.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val auditService: AuditService,
    private val securityUtils: SecurityUtils,
) {

    @Transactional(readOnly = true)
    fun listUsers(page: Int, size: Int): PageResponse<UserResponse> {
        val userPage = userRepository.findAll(PageRequest.of(page, size))
        return PageMapper.from(userPage.map(UserMapper::toResponse))
    }

    @Transactional(readOnly = true)
    fun getUser(id: UUID): UserResponse = UserMapper.toResponse(getUserEntity(id))

    @Transactional(readOnly = true)
    fun getCurrentUser(): UserResponse = UserMapper.toResponse(getCurrentUserEntity())

    @Transactional
    fun updateStatus(id: UUID, request: UpdateUserStatusRequest): UserResponse {
        val user = getUserEntity(id)
        user.enabled = request.enabled
        val saved = userRepository.save(user)
        auditService.log(
            action = AuditAction.USER_STATUS_CHANGED,
            entityType = AuditEntityType.USER,
            entityId = saved.id,
            details = mapOf("enabled" to saved.enabled),
        )
        return UserMapper.toResponse(saved)
    }

    @Transactional
    fun updateRoles(id: UUID, request: UpdateUserRolesRequest): UserResponse {
        val user = getUserEntity(id)
        val roles = roleRepository.findAllByNameIn(request.roles)
        if (roles.size != request.roles.size) {
            val resolvedRoleNames = roles.map { it.name }.toSet()
            val missing = request.roles.filterNot(resolvedRoleNames::contains)
            throw NotFoundException("Roles not found: ${missing.joinToString()}")
        }

        user.roles = roles.toMutableSet()
        val saved = userRepository.save(user)
        auditService.log(
            action = AuditAction.USER_ROLES_CHANGED,
            entityType = AuditEntityType.USER,
            entityId = saved.id,
            details = mapOf("roles" to saved.roles.map { it.name.name }),
        )
        return UserMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getUserEntity(id: UUID): UserEntity = userRepository.findById(id)
        .orElseThrow { NotFoundException("User $id not found") }

    @Transactional(readOnly = true)
    fun getCurrentUserEntity(): UserEntity = getUserEntity(securityUtils.currentUserId())

    @Transactional(readOnly = true)
    fun getUsersByRole(roleName: RoleName): List<UserResponse> {
        return userRepository.findAll()
            .filter { user -> user.roles.any { role -> role.name == roleName } }
            .map(UserMapper::toResponse)
    }
}
