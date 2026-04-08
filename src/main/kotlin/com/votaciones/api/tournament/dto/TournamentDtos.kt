package com.votaciones.api.tournament.dto

import com.votaciones.api.access.domain.TournamentAccessMode
import com.votaciones.api.tournament.domain.TournamentStatus
import com.votaciones.api.tournament.domain.TournamentType
import com.votaciones.api.user.dto.UserSummaryResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class CreateTournamentRequest(
    @field:NotBlank
    @field:Size(max = 160)
    val title: String,
    @field:Size(max = 4000)
    val description: String? = null,
    val type: TournamentType,
    val startAt: Instant? = null,
    val endAt: Instant? = null,
    val accessMode: TournamentAccessMode = TournamentAccessMode.ANONYMOUS,
)

data class UpdateTournamentRequest(
    @field:NotBlank
    @field:Size(max = 160)
    val title: String,
    @field:Size(max = 4000)
    val description: String? = null,
    val type: TournamentType,
    val startAt: Instant? = null,
    val endAt: Instant? = null,
    val accessMode: TournamentAccessMode,
)

data class TournamentResponse(
    val id: UUID,
    val title: String,
    val description: String?,
    val type: TournamentType,
    val status: TournamentStatus,
    val accessMode: TournamentAccessMode,
    val createdBy: UserSummaryResponse,
    val startAt: Instant?,
    val endAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
