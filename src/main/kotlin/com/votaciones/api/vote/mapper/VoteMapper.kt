package com.votaciones.api.vote.mapper

import com.votaciones.api.vote.domain.VoteEntity
import com.votaciones.api.vote.dto.MyVoteResponse
import com.votaciones.api.vote.dto.VoteResponse

object VoteMapper {

    fun toResponse(entity: VoteEntity): VoteResponse = VoteResponse(
        id = entity.id,
        tournamentId = entity.tournament.id,
        roundId = entity.round.id,
        matchId = entity.match.id,
        voterId = entity.voter.id,
        selectedParticipantId = entity.selectedParticipant.id,
        createdAt = entity.createdAt,
    )

    fun toMyVoteResponse(entity: VoteEntity?): MyVoteResponse = MyVoteResponse(
        hasVoted = entity != null,
        selectedParticipantId = entity?.selectedParticipant?.id,
        votedAt = entity?.createdAt,
    )
}
