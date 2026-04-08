package com.votaciones.api.round.dto

import com.votaciones.api.round.domain.RoundStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class CreateRoundRequest(
    @field:NotBlank
    @field:Size(max = 120)
    val name: String,
    @field:Positive
    val roundNumber: Int,
    val opensAt: Instant? = null,
    val closesAt: Instant? = null,
)

data class RoundResponse(
    val id: UUID,
    val tournamentId: UUID,
    val name: String,
    val roundNumber: Int,
    val status: RoundStatus,
    val opensAt: Instant?,
    val closesAt: Instant?,
    val resultsPublishedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
