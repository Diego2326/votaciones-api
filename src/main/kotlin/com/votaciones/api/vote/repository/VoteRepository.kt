package com.votaciones.api.vote.repository

import com.votaciones.api.vote.domain.VoteEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface VoteRepository : JpaRepository<VoteEntity, UUID> {
    fun existsByMatchIdAndJoinSessionId(matchId: UUID, joinSessionId: UUID): Boolean
    fun findByMatchIdAndJoinSessionId(matchId: UUID, joinSessionId: UUID): VoteEntity?
    fun countByMatchId(matchId: UUID): Long

    @Query(
        """
        select v.selectedParticipant.id as participantId, count(v.id) as votes
        from VoteEntity v
        where v.match.id = :matchId
        group by v.selectedParticipant.id
        """,
    )
    fun countVotesByMatchId(@Param("matchId") matchId: UUID): List<VoteCountProjection>
}
