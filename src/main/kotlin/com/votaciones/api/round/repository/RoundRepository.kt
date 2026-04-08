package com.votaciones.api.round.repository

import com.votaciones.api.round.domain.RoundEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RoundRepository : JpaRepository<RoundEntity, UUID> {
    fun findAllByTournamentIdOrderByRoundNumberAsc(tournamentId: UUID): List<RoundEntity>
    fun existsByTournamentIdAndRoundNumber(tournamentId: UUID, roundNumber: Int): Boolean
}
