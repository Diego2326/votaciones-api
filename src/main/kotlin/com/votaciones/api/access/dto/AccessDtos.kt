package com.votaciones.api.access.dto

import com.votaciones.api.access.domain.TournamentAccessMode
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class UpdateTournamentAccessRequest(
    val mode: TournamentAccessMode,
)

data class TournamentAccessResponse(
    val tournamentId: UUID,
    val mode: TournamentAccessMode,
    val joinPin: String,
    val qrToken: String,
    val joinUrl: String,
)

data class JoinByPinRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 12)
    val pin: String,
)

data class JoinByDisplayNameRequest(
    @field:Size(min = 4, max = 12)
    val pin: String? = null,
    val qrToken: String? = null,
    @field:NotBlank
    @field:Size(max = 120)
    val displayName: String,
)

data class JoinByQrRequest(
    @field:NotBlank
    val qrToken: String,
)

data class JoinByEmailPasswordRequest(
    val pin: String? = null,
    val qrToken: String? = null,
    @field:NotBlank
    @field:Email
    val email: String,
    @field:NotBlank
    @field:Size(min = 8, max = 72)
    val password: String,
    @field:Size(max = 100)
    val firstName: String? = null,
    @field:Size(max = 100)
    val lastName: String? = null,
)

data class TournamentJoinSessionResponse(
    val tournamentId: UUID,
    val tournamentTitle: String,
    val mode: TournamentAccessMode,
    val sessionToken: String,
    val displayName: String?,
    val userId: UUID?,
    val joinedAt: Instant,
    val expiresAt: Instant?,
)

data class SessionSummaryResponse(
    val sessionId: UUID,
    val tournamentId: UUID,
    val displayName: String?,
    val userId: UUID?,
    val joinedAt: Instant,
    val lastSeenAt: Instant,
)
