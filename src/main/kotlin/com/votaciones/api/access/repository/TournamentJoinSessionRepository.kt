package com.votaciones.api.access.repository

import com.votaciones.api.access.domain.TournamentJoinSessionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TournamentJoinSessionRepository : JpaRepository<TournamentJoinSessionEntity, UUID> {
    fun findBySessionTokenHashAndActiveTrue(sessionTokenHash: String): TournamentJoinSessionEntity?
    fun findAllByTournamentIdAndActiveTrueOrderByCreatedAtDesc(tournamentId: UUID): List<TournamentJoinSessionEntity>
}
