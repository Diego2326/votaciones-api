package com.votaciones.api.user.dto

import com.votaciones.api.user.domain.RoleName
import jakarta.validation.constraints.NotEmpty
import java.time.Instant
import java.util.UUID

data class UserSummaryResponse(
    val id: UUID,
    val username: String,
    val fullName: String,
)

data class UserResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val enabled: Boolean,
    val roles: Set<RoleName>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class UpdateUserStatusRequest(
    val enabled: Boolean,
)

data class UpdateUserRolesRequest(
    @field:NotEmpty
    val roles: Set<RoleName>,
)
