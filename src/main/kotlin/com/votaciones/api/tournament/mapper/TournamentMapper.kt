package com.votaciones.api.tournament.mapper

import com.votaciones.api.tournament.domain.TournamentEntity
import com.votaciones.api.tournament.dto.TournamentResponse
import com.votaciones.api.user.mapper.UserMapper

object TournamentMapper {

    fun toResponse(entity: TournamentEntity): TournamentResponse = TournamentResponse(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        type = entity.type,
        status = entity.status,
        createdBy = UserMapper.toSummary(entity.createdBy),
        startAt = entity.startAt,
        endAt = entity.endAt,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )
}
