package com.votaciones.api.audit.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import com.votaciones.api.audit.domain.AuditLogEntity
import com.votaciones.api.audit.mapper.AuditMapper
import com.votaciones.api.audit.dto.AuditLogResponse
import com.votaciones.api.audit.repository.AuditLogRepository
import com.votaciones.api.common.dto.PageResponse
import com.votaciones.api.common.util.PageMapper
import com.votaciones.api.security.SecurityUtils
import com.votaciones.api.user.domain.UserEntity
import com.votaciones.api.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuditService(
    private val auditLogRepository: AuditLogRepository,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
    private val securityUtils: SecurityUtils,
) {

    @Transactional
    fun log(
        action: AuditAction,
        entityType: AuditEntityType,
        entityId: UUID? = null,
        tournamentId: UUID? = null,
        details: Any? = null,
        user: UserEntity? = null,
    ) {
        val auditLog = AuditLogEntity(
            user = user ?: resolveCurrentUser(),
            action = action,
            entityType = entityType,
            tournamentId = tournamentId,
            entityId = entityId,
            detailsJson = details?.let { serializeDetails(it) },
        )
        auditLogRepository.save(auditLog)
    }

    @Transactional(readOnly = true)
    fun listAll(page: Int, size: Int): PageResponse<AuditLogResponse> {
        val auditPage = auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
        return PageMapper.from(auditPage.map(AuditMapper::toResponse))
    }

    @Transactional(readOnly = true)
    fun listByTournament(tournamentId: UUID, page: Int, size: Int): PageResponse<AuditLogResponse> {
        val auditPage = auditLogRepository.findAllByTournamentIdOrderByCreatedAtDesc(tournamentId, PageRequest.of(page, size))
        return PageMapper.from(auditPage.map(AuditMapper::toResponse))
    }

    private fun resolveCurrentUser(): UserEntity? = try {
        userRepository.findById(securityUtils.currentUserId()).orElse(null)
    } catch (_: Exception) {
        null
    }

    private fun serializeDetails(details: Any): String = runCatching {
        objectMapper.writeValueAsString(details)
    }.getOrElse {
        details.toString()
    }
}
