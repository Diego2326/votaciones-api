package com.votaciones.api.round.mapper

import com.votaciones.api.round.domain.RoundEntity
import com.votaciones.api.round.dto.RoundResponse

object RoundMapper {

    fun toResponse(entity: RoundEntity): RoundResponse = RoundResponse(
        id = entity.id,
        tournamentId = entity.tournament.id,
        name = entity.name,
        roundNumber = entity.roundNumber,
        status = entity.status,
        opensAt = entity.opensAt,
        closesAt = entity.closesAt,
        resultsPublishedAt = entity.resultsPublishedAt,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )
}
