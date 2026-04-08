package com.votaciones.api.participant.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class CreateParticipantRequest(
    @field:NotBlank
    @field:Size(max = 160)
    val name: String,
    @field:Size(max = 4000)
    val description: String? = null,
    @field:Size(max = 500)
    val imageUrl: String? = null,
    val active: Boolean = true,
)

data class UpdateParticipantRequest(
    @field:NotBlank
    @field:Size(max = 160)
    val name: String,
    @field:Size(max = 4000)
    val description: String? = null,
    @field:Size(max = 500)
    val imageUrl: String? = null,
    val active: Boolean = true,
)

data class ParticipantSummaryResponse(
    val id: UUID,
    val name: String,
    val imageUrl: String?,
    val active: Boolean,
)

data class ParticipantResponse(
    val id: UUID,
    val tournamentId: UUID,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
