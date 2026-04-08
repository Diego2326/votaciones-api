package com.votaciones.api.round.service

import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import com.votaciones.api.audit.service.AuditService
import com.votaciones.api.common.exception.BadRequestException
import com.votaciones.api.common.exception.NotFoundException
import com.votaciones.api.match.domain.MatchStatus
import com.votaciones.api.match.repository.MatchRepository
import com.votaciones.api.round.domain.RoundEntity
import com.votaciones.api.round.domain.RoundStatus
import com.votaciones.api.round.dto.CreateRoundRequest
import com.votaciones.api.round.dto.RoundResponse
import com.votaciones.api.round.mapper.RoundMapper
import com.votaciones.api.round.repository.RoundRepository
import com.votaciones.api.security.AuthorizationService
import com.votaciones.api.tournament.service.TournamentService
import com.votaciones.api.vote.repository.VoteRepository
import com.votaciones.api.websocket.service.RealtimeEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class RoundService(
    private val roundRepository: RoundRepository,
    private val matchRepository: MatchRepository,
    private val voteRepository: VoteRepository,
    private val tournamentService: TournamentService,
    private val authorizationService: AuthorizationService,
    private val auditService: AuditService,
    private val realtimeEventPublisher: RealtimeEventPublisher,
) {

    @Transactional
    fun create(tournamentId: UUID, request: CreateRoundRequest): RoundResponse {
        val tournament = tournamentService.getEntity(tournamentId)
        authorizationService.assertCanManageTournament(tournament)
        tournamentService.assertEditable(tournament)
        validateDateWindow(request.opensAt, request.closesAt)

        if (roundRepository.existsByTournamentIdAndRoundNumber(tournamentId, request.roundNumber)) {
            throw BadRequestException("Round number ${request.roundNumber} already exists for tournament $tournamentId")
        }

        val round = roundRepository.save(
            RoundEntity(
                tournament = tournament,
                name = request.name.trim(),
                roundNumber = request.roundNumber,
                opensAt = request.opensAt,
                closesAt = request.closesAt,
            ),
        )
        auditService.log(
            action = AuditAction.ROUND_CREATED,
            entityType = AuditEntityType.ROUND,
            entityId = round.id,
            tournamentId = tournament.id,
            details = mapOf("name" to round.name, "roundNumber" to round.roundNumber),
        )
        return RoundMapper.toResponse(round)
    }

    @Transactional(readOnly = true)
    fun listByTournament(tournamentId: UUID): List<RoundResponse> = roundRepository
        .findAllByTournamentIdOrderByRoundNumberAsc(tournamentId)
        .map(RoundMapper::toResponse)

    @Transactional(readOnly = true)
    fun getById(id: UUID): RoundResponse = RoundMapper.toResponse(getEntity(id))

    @Transactional
    fun open(id: UUID): RoundResponse {
        val round = getEntity(id)
        authorizationService.assertCanManageTournament(round.tournament)
        tournamentService.assertActiveForVoting(round.tournament)
        if (round.status != RoundStatus.PENDING) {
            throw BadRequestException("Only PENDING rounds can be opened")
        }

        round.status = RoundStatus.OPEN
        round.opensAt = round.opensAt ?: Instant.now()
        matchRepository.findAllByRoundIdOrderByCreatedAtAsc(round.id).forEach { match ->
            if (match.status == MatchStatus.PENDING) {
                match.status = MatchStatus.OPEN
                matchRepository.save(match)
            }
        }
        val saved = roundRepository.save(round)
        auditService.log(
            action = AuditAction.ROUND_OPENED,
            entityType = AuditEntityType.ROUND,
            entityId = saved.id,
            tournamentId = saved.tournament.id,
        )
        realtimeEventPublisher.publishRoundOpened(saved.tournament.id, saved.id)
        return RoundMapper.toResponse(saved)
    }

    @Transactional
    fun close(id: UUID): RoundResponse {
        val round = getEntity(id)
        authorizationService.assertCanManageTournament(round.tournament)
        if (round.status != RoundStatus.OPEN) {
            throw BadRequestException("Only OPEN rounds can be closed")
        }

        round.status = RoundStatus.CLOSED
        round.closesAt = Instant.now()
        matchRepository.findAllByRoundIdOrderByCreatedAtAsc(round.id).forEach { match ->
            if (match.status == MatchStatus.OPEN) {
                match.status = MatchStatus.CLOSED
                matchRepository.save(match)
            }
        }
        val saved = roundRepository.save(round)
        auditService.log(
            action = AuditAction.ROUND_CLOSED,
            entityType = AuditEntityType.ROUND,
            entityId = saved.id,
            tournamentId = saved.tournament.id,
        )
        realtimeEventPublisher.publishRoundClosed(saved.tournament.id, saved.id)
        return RoundMapper.toResponse(saved)
    }

    @Transactional
    fun process(id: UUID): RoundResponse {
        val round = getEntity(id)
        authorizationService.assertCanManageTournament(round.tournament)
        if (round.status !in setOf(RoundStatus.CLOSED, RoundStatus.PROCESSING)) {
            throw BadRequestException("Round must be CLOSED before processing")
        }

        val matches = matchRepository.findAllByRoundIdOrderByCreatedAtAsc(round.id)
        matches.forEach { match ->
            if (match.winner != null) {
                match.status = MatchStatus.RESOLVED
                matchRepository.save(match)
                return@forEach
            }

            val counts = voteRepository.countVotesByMatchId(match.id).associate { it.participantId to it.votes }
            val votesA = counts[match.participantA.id] ?: 0L
            val votesB = counts[match.participantB.id] ?: 0L

            when {
                votesA > votesB -> {
                    match.winner = match.participantA
                    match.status = MatchStatus.RESOLVED
                }

                votesB > votesA -> {
                    match.winner = match.participantB
                    match.status = MatchStatus.RESOLVED
                }

                else -> {
                    match.winner = null
                    match.status = MatchStatus.TIED
                }
            }

            matchRepository.save(match)
        }

        round.status = RoundStatus.PROCESSING
        val saved = roundRepository.save(round)
        auditService.log(
            action = AuditAction.ROUND_PROCESSED,
            entityType = AuditEntityType.ROUND,
            entityId = saved.id,
            tournamentId = saved.tournament.id,
        )
        return RoundMapper.toResponse(saved)
    }

    @Transactional
    fun publishResults(id: UUID): RoundResponse {
        val round = getEntity(id)
        authorizationService.assertCanManageTournament(round.tournament)
        if (round.status != RoundStatus.PROCESSING) {
            throw BadRequestException("Round must be PROCESSING before publishing results")
        }

        val unresolved = matchRepository.findAllByRoundIdOrderByCreatedAtAsc(round.id)
            .filter { it.status != MatchStatus.RESOLVED || it.winner == null }
        if (unresolved.isNotEmpty()) {
            throw BadRequestException("All matches must have resolved winners before publishing results")
        }

        round.status = RoundStatus.PUBLISHED
        round.resultsPublishedAt = Instant.now()
        val saved = roundRepository.save(round)
        auditService.log(
            action = AuditAction.RESULTS_PUBLISHED,
            entityType = AuditEntityType.ROUND,
            entityId = saved.id,
            tournamentId = saved.tournament.id,
        )
        realtimeEventPublisher.publishResultsPublished(saved.tournament.id, saved.id)
        return RoundMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getEntity(id: UUID): RoundEntity = roundRepository.findById(id)
        .orElseThrow { NotFoundException("Round $id not found") }

    private fun validateDateWindow(opensAt: Instant?, closesAt: Instant?) {
        if (opensAt != null && closesAt != null && closesAt.isBefore(opensAt)) {
            throw BadRequestException("Round closesAt cannot be before opensAt")
        }
    }
}
