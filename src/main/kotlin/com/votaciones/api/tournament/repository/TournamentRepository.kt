package com.votaciones.api.tournament.repository

import com.votaciones.api.tournament.domain.TournamentEntity
import com.votaciones.api.tournament.domain.TournamentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TournamentRepository : JpaRepository<TournamentEntity, UUID> {
    fun findAllByStatus(status: TournamentStatus, pageable: Pageable): Page<TournamentEntity>
    fun findAllByCreatedById(createdById: UUID, pageable: Pageable): Page<TournamentEntity>
    fun findByJoinPin(joinPin: String): TournamentEntity?
    fun findByQrToken(qrToken: String): TournamentEntity?
}
