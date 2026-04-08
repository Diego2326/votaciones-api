package com.votaciones.api.match.repository

import com.votaciones.api.match.domain.MatchEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MatchRepository : JpaRepository<MatchEntity, UUID> {
    fun findAllByRoundIdOrderByCreatedAtAsc(roundId: UUID): List<MatchEntity>
}
