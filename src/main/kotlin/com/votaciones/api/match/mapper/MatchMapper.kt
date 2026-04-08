package com.votaciones.api.match.mapper

import com.votaciones.api.match.domain.MatchEntity
import com.votaciones.api.match.dto.MatchResponse
import com.votaciones.api.participant.mapper.ParticipantMapper

object MatchMapper {

    fun toResponse(entity: MatchEntity): MatchResponse = MatchResponse(
        id = entity.id,
        roundId = entity.round.id,
        participantA = ParticipantMapper.toSummary(entity.participantA),
        participantB = ParticipantMapper.toSummary(entity.participantB),
        winner = entity.winner?.let { ParticipantMapper.toSummary(it) },
        status = entity.status,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )
}
