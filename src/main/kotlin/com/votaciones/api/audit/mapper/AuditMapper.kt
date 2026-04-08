package com.votaciones.api.audit.mapper

import com.votaciones.api.audit.domain.AuditLogEntity
import com.votaciones.api.audit.dto.AuditLogResponse

object AuditMapper {

    fun toResponse(entity: AuditLogEntity): AuditLogResponse = AuditLogResponse(
        id = entity.id,
        userId = entity.user?.id,
        username = entity.user?.username,
        action = entity.action,
        entityType = entity.entityType,
        tournamentId = entity.tournamentId,
        entityId = entity.entityId,
        detailsJson = entity.detailsJson,
        createdAt = entity.createdAt,
    )
}
