package com.votaciones.api.participant.mapper

import com.votaciones.api.participant.domain.ParticipantEntity
import com.votaciones.api.participant.dto.ParticipantResponse
import com.votaciones.api.participant.dto.ParticipantSummaryResponse

object ParticipantMapper {

    fun toResponse(entity: ParticipantEntity): ParticipantResponse = ParticipantResponse(
        id = entity.id,
        tournamentId = entity.tournament.id,
        name = entity.name,
        description = entity.description,
        imageUrl = entity.imageUrl,
        active = entity.active,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toSummary(entity: ParticipantEntity): ParticipantSummaryResponse = ParticipantSummaryResponse(
        id = entity.id,
        name = entity.name,
        imageUrl = entity.imageUrl,
        active = entity.active,
    )
}
