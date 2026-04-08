package com.votaciones.api.audit.dto

import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import java.time.Instant
import java.util.UUID

data class AuditLogResponse(
    val id: UUID,
    val userId: UUID?,
    val username: String?,
    val action: AuditAction,
    val entityType: AuditEntityType,
    val tournamentId: UUID?,
    val entityId: UUID?,
    val detailsJson: String?,
    val createdAt: Instant,
)
