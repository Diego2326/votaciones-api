package com.votaciones.api.user.mapper

import com.votaciones.api.user.domain.UserEntity
import com.votaciones.api.user.dto.UserResponse
import com.votaciones.api.user.dto.UserSummaryResponse

object UserMapper {

    fun toResponse(entity: UserEntity): UserResponse = UserResponse(
        id = entity.id,
        username = entity.username,
        email = entity.email,
        firstName = entity.firstName,
        lastName = entity.lastName,
        enabled = entity.enabled,
        roles = entity.roles.map { it.name }.toSet(),
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toSummary(entity: UserEntity): UserSummaryResponse = UserSummaryResponse(
        id = entity.id,
        username = entity.username,
        fullName = "${entity.firstName} ${entity.lastName}".trim(),
    )
}
