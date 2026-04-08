package com.votaciones.api.vote.dto

import com.votaciones.api.match.domain.MatchStatus
import com.votaciones.api.round.domain.RoundStatus
import com.votaciones.api.tournament.domain.TournamentStatus
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class CastVoteRequest(
    @field:NotNull
    val selectedParticipantId: UUID,
)

data class VoteResponse(
    val id: UUID,
    val tournamentId: UUID,
    val roundId: UUID,
    val matchId: UUID,
    val voterId: UUID,
    val selectedParticipantId: UUID,
    val createdAt: Instant,
)

data class MyVoteResponse(
    val hasVoted: Boolean,
    val selectedParticipantId: UUID?,
    val votedAt: Instant?,
)

data class ParticipantVoteCountResponse(
    val participantId: UUID,
    val participantName: String,
    val votes: Long,
)

data class MatchResultsResponse(
    val matchId: UUID,
    val status: MatchStatus,
    val winnerId: UUID?,
    val totalVotes: Long,
    val results: List<ParticipantVoteCountResponse>,
)

data class RoundResultsResponse(
    val roundId: UUID,
    val tournamentId: UUID,
    val status: RoundStatus,
    val matches: List<MatchResultsResponse>,
)

data class TournamentResultsResponse(
    val tournamentId: UUID,
    val status: TournamentStatus,
    val rounds: List<RoundResultsResponse>,
)
