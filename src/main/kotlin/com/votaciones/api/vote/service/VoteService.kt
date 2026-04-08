package com.votaciones.api.vote.service

import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import com.votaciones.api.audit.service.AuditService
import com.votaciones.api.common.exception.BadRequestException
import com.votaciones.api.common.exception.ConflictException
import com.votaciones.api.match.domain.MatchEntity
import com.votaciones.api.match.domain.MatchStatus
import com.votaciones.api.match.repository.MatchRepository
import com.votaciones.api.match.service.MatchService
import com.votaciones.api.round.domain.RoundStatus
import com.votaciones.api.round.repository.RoundRepository
import com.votaciones.api.security.AuthorizationService
import com.votaciones.api.tournament.service.TournamentService
import com.votaciones.api.user.service.UserService
import com.votaciones.api.vote.domain.VoteEntity
import com.votaciones.api.vote.dto.CastVoteRequest
import com.votaciones.api.vote.dto.MatchResultsResponse
import com.votaciones.api.vote.dto.MyVoteResponse
import com.votaciones.api.vote.dto.ParticipantVoteCountResponse
import com.votaciones.api.vote.dto.RoundResultsResponse
import com.votaciones.api.vote.dto.TournamentResultsResponse
import com.votaciones.api.vote.dto.VoteResponse
import com.votaciones.api.vote.mapper.VoteMapper
import com.votaciones.api.vote.repository.VoteRepository
import com.votaciones.api.websocket.service.RealtimeEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class VoteService(
    private val voteRepository: VoteRepository,
    private val matchRepository: MatchRepository,
    private val matchService: MatchService,
    private val roundRepository: RoundRepository,
    private val tournamentService: TournamentService,
    private val userService: UserService,
    private val authorizationService: AuthorizationService,
    private val auditService: AuditService,
    private val realtimeEventPublisher: RealtimeEventPublisher,
) {

    @Transactional
    fun castVote(matchId: UUID, request: CastVoteRequest): VoteResponse {
        authorizationService.assertCanVote()
        val currentUser = userService.getCurrentUserEntity()
        val match = matchService.getEntity(matchId)
        validateVoteState(match)

        if (voteRepository.existsByMatchIdAndVoterId(matchId, currentUser.id)) {
            throw ConflictException("User has already voted on this match")
        }

        val selectedParticipant = when (request.selectedParticipantId) {
            match.participantA.id -> match.participantA
            match.participantB.id -> match.participantB
            else -> throw BadRequestException("Selected participant does not belong to the match")
        }

        val vote = try {
            voteRepository.save(
                VoteEntity(
                    tournament = match.round.tournament,
                    round = match.round,
                    match = match,
                    voter = currentUser,
                    selectedParticipant = selectedParticipant,
                ),
            )
        } catch (_: DataIntegrityViolationException) {
            throw ConflictException("User has already voted on this match")
        }

        auditService.log(
            action = AuditAction.VOTE_CAST,
            entityType = AuditEntityType.VOTE,
            entityId = vote.id,
            tournamentId = match.round.tournament.id,
            details = mapOf(
                "matchId" to match.id,
                "selectedParticipantId" to selectedParticipant.id,
            ),
        )

        realtimeEventPublisher.publishVoteCountUpdated(
            tournamentId = match.round.tournament.id,
            roundId = match.round.id,
            matchId = match.id,
            totalVotes = voteRepository.countByMatchId(match.id),
        )

        return VoteMapper.toResponse(vote)
    }

    @Transactional(readOnly = true)
    fun getMyVote(matchId: UUID): MyVoteResponse {
        val user = userService.getCurrentUserEntity()
        return VoteMapper.toMyVoteResponse(voteRepository.findByMatchIdAndVoterId(matchId, user.id))
    }

    @Transactional(readOnly = true)
    fun getMatchResults(matchId: UUID): MatchResultsResponse = buildMatchResults(matchService.getEntity(matchId))

    @Transactional(readOnly = true)
    fun getRoundResults(roundId: UUID): RoundResultsResponse {
        val round = roundRepository.findById(roundId)
            .orElseThrow { BadRequestException("Round $roundId not found") }
        val matches = matchRepository.findAllByRoundIdOrderByCreatedAtAsc(roundId)
            .map(::buildMatchResults)
        return RoundResultsResponse(
            roundId = round.id,
            tournamentId = round.tournament.id,
            status = round.status,
            matches = matches,
        )
    }

    @Transactional(readOnly = true)
    fun getTournamentResults(tournamentId: UUID): TournamentResultsResponse {
        val tournament = tournamentService.getEntity(tournamentId)
        val rounds = roundRepository.findAllByTournamentIdOrderByRoundNumberAsc(tournamentId)
            .map { round ->
                RoundResultsResponse(
                    roundId = round.id,
                    tournamentId = round.tournament.id,
                    status = round.status,
                    matches = matchRepository.findAllByRoundIdOrderByCreatedAtAsc(round.id).map(::buildMatchResults),
                )
            }
        return TournamentResultsResponse(
            tournamentId = tournament.id,
            status = tournament.status,
            rounds = rounds,
        )
    }

    private fun validateVoteState(match: MatchEntity) {
        tournamentService.assertActiveForVoting(match.round.tournament)
        if (match.round.status != RoundStatus.OPEN) {
            throw BadRequestException("Round must be OPEN to accept votes")
        }
        if (match.status != MatchStatus.OPEN) {
            throw BadRequestException("Match must be OPEN to accept votes")
        }
    }

    private fun buildMatchResults(match: MatchEntity): MatchResultsResponse {
        val counts = voteRepository.countVotesByMatchId(match.id).associate { it.participantId to it.votes }
        val results = listOf(match.participantA, match.participantB).map { participant ->
            ParticipantVoteCountResponse(
                participantId = participant.id,
                participantName = participant.name,
                votes = counts[participant.id] ?: 0L,
            )
        }
        return MatchResultsResponse(
            matchId = match.id,
            status = match.status,
            winnerId = match.winner?.id,
            totalVotes = results.sumOf { it.votes },
            results = results,
        )
    }
}
