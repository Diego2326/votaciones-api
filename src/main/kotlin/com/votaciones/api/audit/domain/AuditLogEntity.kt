package com.votaciones.api.audit.domain

import com.votaciones.api.common.entity.BaseEntity
import com.votaciones.api.user.domain.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "audit_logs",
    indexes = [
        Index(name = "idx_audit_user", columnList = "user_id"),
        Index(name = "idx_audit_tournament_id", columnList = "tournament_id"),
        Index(name = "idx_audit_entity_type", columnList = "entity_type"),
        Index(name = "idx_audit_entity_id", columnList = "entity_id"),
        Index(name = "idx_audit_action", columnList = "action"),
        Index(name = "idx_audit_created_at", columnList = "created_at"),
    ],
)
class AuditLogEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    var action: AuditAction,
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 32)
    var entityType: AuditEntityType,
    @Column(name = "tournament_id")
    var tournamentId: UUID? = null,
    @Column(name = "entity_id")
    var entityId: UUID? = null,
    @Column(name = "details_json", columnDefinition = "TEXT")
    var detailsJson: String? = null,
) : BaseEntity()
