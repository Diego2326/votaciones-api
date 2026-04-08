package com.votaciones.api.match.dto

import com.votaciones.api.match.domain.MatchStatus
import com.votaciones.api.participant.dto.ParticipantSummaryResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class CreateMatchRequest(
    @field:NotNull
    val participantAId: UUID,
    @field:NotNull
    val participantBId: UUID,
)

data class CreateMatchesRequest(
    val autoGenerate: Boolean = false,
    @field:Valid
    val matches: List<CreateMatchRequest> = emptyList(),
)

data class RegisterWinnerRequest(
    @field:NotNull
    val winnerId: UUID,
)

data class MatchResponse(
    val id: UUID,
    val roundId: UUID,
    val participantA: ParticipantSummaryResponse,
    val participantB: ParticipantSummaryResponse,
    val winner: ParticipantSummaryResponse?,
    val status: MatchStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)
