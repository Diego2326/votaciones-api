package com.votaciones.api.participant.repository

import com.votaciones.api.participant.domain.ParticipantEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ParticipantRepository : JpaRepository<ParticipantEntity, UUID> {
    fun findAllByTournamentIdOrderByCreatedAtAsc(tournamentId: UUID): List<ParticipantEntity>
    fun countByTournamentIdAndActiveTrue(tournamentId: UUID): Long
}
