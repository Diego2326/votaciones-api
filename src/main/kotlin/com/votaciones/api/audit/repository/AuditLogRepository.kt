package com.votaciones.api.audit.repository

import com.votaciones.api.audit.domain.AuditLogEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AuditLogRepository : JpaRepository<AuditLogEntity, UUID> {
    fun findAllByTournamentIdOrderByCreatedAtDesc(tournamentId: UUID, pageable: Pageable): Page<AuditLogEntity>
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<AuditLogEntity>
}
