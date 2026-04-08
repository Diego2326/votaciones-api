package com.votaciones.api.match.service

import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import com.votaciones.api.audit.service.AuditService
import com.votaciones.api.common.exception.BadRequestException
import com.votaciones.api.common.exception.NotFoundException
import com.votaciones.api.match.domain.MatchEntity
import com.votaciones.api.match.domain.MatchStatus
import com.votaciones.api.match.dto.CreateMatchRequest
import com.votaciones.api.match.dto.CreateMatchesRequest
import com.votaciones.api.match.dto.MatchResponse
import com.votaciones.api.match.dto.RegisterWinnerRequest
import com.votaciones.api.match.mapper.MatchMapper
import com.votaciones.api.match.repository.MatchRepository
import com.votaciones.api.participant.domain.ParticipantEntity
import com.votaciones.api.participant.service.ParticipantService
import com.votaciones.api.round.domain.RoundStatus
import com.votaciones.api.round.service.RoundService
import com.votaciones.api.security.AuthorizationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val roundService: RoundService,
    private val participantService: ParticipantService,
    private val authorizationService: AuthorizationService,
    private val auditService: AuditService,
) {

    @Transactional
    fun create(roundId: UUID, request: CreateMatchesRequest): List<MatchResponse> {
        val round = roundService.getEntity(roundId)
        authorizationService.assertCanManageTournament(round.tournament)
        if (round.status != RoundStatus.PENDING) {
            throw BadRequestException("Matches can only be created while round is PENDING")
        }

        val specs = when {
            request.autoGenerate -> generateRoundRobinPairs(round.tournament.id)
            request.matches.isNotEmpty() -> request.matches
            else -> throw BadRequestException("Provide matches or set autoGenerate=true")
        }

        val matches = specs.map { spec ->
            validateMatchSpec(round.tournament.id, spec)
            val participantA = participantService.getEntity(spec.participantAId)
            val participantB = participantService.getEntity(spec.participantBId)
            matchRepository.save(
                MatchEntity(
                    round = round,
                    participantA = participantA,
                    participantB = participantB,
                    status = MatchStatus.PENDING,
                ),
            )
        }

        matches.forEach { match ->
            auditService.log(
                action = AuditAction.MATCH_CREATED,
                entityType = AuditEntityType.MATCH,
                entityId = match.id,
                tournamentId = round.tournament.id,
                details = mapOf(
                    "participantAId" to match.participantA.id,
                    "participantBId" to match.participantB.id,
                ),
            )
        }
        return matches.map(MatchMapper::toResponse)
    }

    @Transactional(readOnly = true)
    fun listByRound(roundId: UUID): List<MatchResponse> = matchRepository
        .findAllByRoundIdOrderByCreatedAtAsc(roundId)
        .map(MatchMapper::toResponse)

    @Transactional(readOnly = true)
    fun getById(id: UUID): MatchResponse = MatchMapper.toResponse(getEntity(id))

    @Transactional
    fun registerWinner(id: UUID, request: RegisterWinnerRequest): MatchResponse {
        val match = getEntity(id)
        authorizationService.assertCanManageTournament(match.round.tournament)
        val winner = when (request.winnerId) {
            match.participantA.id -> match.participantA
            match.participantB.id -> match.participantB
            else -> throw BadRequestException("Winner must belong to the match")
        }

        match.winner = winner
        match.status = MatchStatus.RESOLVED
        val saved = matchRepository.save(match)
        auditService.log(
            action = AuditAction.MATCH_WINNER_REGISTERED,
            entityType = AuditEntityType.MATCH,
            entityId = saved.id,
            tournamentId = saved.round.tournament.id,
            details = mapOf("winnerId" to winner.id),
        )
        return MatchMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getEntity(id: UUID): MatchEntity = matchRepository.findById(id)
        .orElseThrow { NotFoundException("Match $id not found") }

    private fun validateMatchSpec(tournamentId: UUID, spec: CreateMatchRequest) {
        if (spec.participantAId == spec.participantBId) {
            throw BadRequestException("A match cannot contain the same participant twice")
        }
        val participantA = participantService.getEntity(spec.participantAId)
        val participantB = participantService.getEntity(spec.participantBId)
        validateParticipantOwnership(tournamentId, participantA)
        validateParticipantOwnership(tournamentId, participantB)
    }

    private fun validateParticipantOwnership(tournamentId: UUID, participant: ParticipantEntity) {
        if (participant.tournament.id != tournamentId) {
            throw BadRequestException("Participant ${participant.id} does not belong to this tournament")
        }
        if (!participant.active) {
            throw BadRequestException("Participant ${participant.id} is inactive")
        }
    }

    private fun generateRoundRobinPairs(tournamentId: UUID): List<CreateMatchRequest> {
        val participants = participantService.listByTournament(tournamentId)
            .filter { it.active }

        if (participants.size < 2 || participants.size % 2 != 0) {
            throw BadRequestException("Auto-generation requires an even number of active participants")
        }

        return participants.chunked(2).map {
            CreateMatchRequest(
                participantAId = it[0].id,
                participantBId = it[1].id,
            )
        }
    }
}
