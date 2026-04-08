package com.votaciones.api.tournament.service

import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import com.votaciones.api.audit.service.AuditService
import com.votaciones.api.common.dto.PageResponse
import com.votaciones.api.common.exception.BadRequestException
import com.votaciones.api.common.exception.NotFoundException
import com.votaciones.api.common.util.PageMapper
import com.votaciones.api.participant.repository.ParticipantRepository
import com.votaciones.api.security.AuthorizationService
import com.votaciones.api.tournament.domain.TournamentEntity
import com.votaciones.api.tournament.domain.TournamentStatus
import com.votaciones.api.tournament.dto.CreateTournamentRequest
import com.votaciones.api.tournament.dto.TournamentResponse
import com.votaciones.api.tournament.dto.UpdateTournamentRequest
import com.votaciones.api.tournament.mapper.TournamentMapper
import com.votaciones.api.tournament.repository.TournamentRepository
import com.votaciones.api.user.service.UserService
import com.votaciones.api.websocket.service.RealtimeEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TournamentService(
    private val tournamentRepository: TournamentRepository,
    private val participantRepository: ParticipantRepository,
    private val userService: UserService,
    private val authorizationService: AuthorizationService,
    private val auditService: AuditService,
    private val realtimeEventPublisher: RealtimeEventPublisher,
    private val tournamentSessionTokenService: com.votaciones.api.access.service.TournamentSessionTokenService,
) {

    @Transactional
    fun create(request: CreateTournamentRequest): TournamentResponse {
        validateDateWindow(request.startAt, request.endAt)
        val currentUser = userService.getCurrentUserEntity()
        val tournament = tournamentRepository.save(
            TournamentEntity(
                title = request.title.trim(),
                description = request.description?.trim(),
                type = request.type,
                createdBy = currentUser,
                startAt = request.startAt,
                endAt = request.endAt,
                accessMode = request.accessMode,
                joinPin = tournamentSessionTokenService.randomPin(),
                qrToken = tournamentSessionTokenService.randomToken(),
            ),
        )

        auditService.log(
            action = AuditAction.TOURNAMENT_CREATED,
            entityType = AuditEntityType.TOURNAMENT,
            entityId = tournament.id,
            tournamentId = tournament.id,
            details = mapOf("title" to tournament.title, "type" to tournament.type.name),
        )
        realtimeEventPublisher.publishTournamentUpdated(tournament.id, "Tournament created")
        return TournamentMapper.toResponse(tournament)
    }

    @Transactional(readOnly = true)
    fun list(status: TournamentStatus?, page: Int, size: Int): PageResponse<TournamentResponse> {
        val pageable = PageRequest.of(page, size)
        val tournaments = status?.let { tournamentRepository.findAllByStatus(it, pageable) }
            ?: tournamentRepository.findAll(pageable)
        return PageMapper.from(tournaments.map(TournamentMapper::toResponse))
    }

    @Transactional(readOnly = true)
    fun getById(id: UUID): TournamentResponse = TournamentMapper.toResponse(getEntity(id))

    @Transactional
    fun update(id: UUID, request: UpdateTournamentRequest): TournamentResponse {
        validateDateWindow(request.startAt, request.endAt)
        val tournament = getEntity(id)
        authorizationService.assertCanManageTournament(tournament)
        assertEditable(tournament)

        tournament.title = request.title.trim()
        tournament.description = request.description?.trim()
        tournament.type = request.type
        tournament.startAt = request.startAt
        tournament.endAt = request.endAt
        tournament.accessMode = request.accessMode

        val saved = tournamentRepository.save(tournament)
        auditService.log(
            action = AuditAction.TOURNAMENT_UPDATED,
            entityType = AuditEntityType.TOURNAMENT,
            entityId = saved.id,
            tournamentId = saved.id,
            details = mapOf("title" to saved.title, "status" to saved.status.name),
        )
        realtimeEventPublisher.publishTournamentUpdated(saved.id, "Tournament updated")
        return TournamentMapper.toResponse(saved)
    }

    @Transactional
    fun publish(id: UUID): TournamentResponse = transitionStatus(
        id = id,
        allowedCurrentStatuses = setOf(TournamentStatus.DRAFT),
        nextStatus = TournamentStatus.PUBLISHED,
    ) { tournament ->
        if (participantRepository.countByTournamentIdAndActiveTrue(tournament.id) < 2) {
            throw BadRequestException("At least two active participants are required to publish a tournament")
        }
    }

    @Transactional
    fun activate(id: UUID): TournamentResponse = transitionStatus(
        id = id,
        allowedCurrentStatuses = setOf(TournamentStatus.PUBLISHED, TournamentStatus.PAUSED),
        nextStatus = TournamentStatus.ACTIVE,
    )

    @Transactional
    fun pause(id: UUID): TournamentResponse = transitionStatus(
        id = id,
        allowedCurrentStatuses = setOf(TournamentStatus.ACTIVE),
        nextStatus = TournamentStatus.PAUSED,
    )

    @Transactional
    fun close(id: UUID): TournamentResponse = transitionStatus(
        id = id,
        allowedCurrentStatuses = setOf(TournamentStatus.ACTIVE, TournamentStatus.PAUSED, TournamentStatus.PUBLISHED),
        nextStatus = TournamentStatus.CLOSED,
    )

    @Transactional(readOnly = true)
    fun getEntity(id: UUID): TournamentEntity = tournamentRepository.findById(id)
        .orElseThrow { NotFoundException("Tournament $id not found") }

    fun assertEditable(tournament: TournamentEntity) {
        if (tournament.status in setOf(TournamentStatus.ACTIVE, TournamentStatus.CLOSED, TournamentStatus.FINISHED, TournamentStatus.CANCELLED)) {
            throw BadRequestException("Tournament in status ${tournament.status} cannot be modified")
        }
    }

    fun assertActiveForVoting(tournament: TournamentEntity) {
        if (tournament.status != TournamentStatus.ACTIVE) {
            throw BadRequestException("Tournament must be ACTIVE to accept votes")
        }
    }

    private fun transitionStatus(
        id: UUID,
        allowedCurrentStatuses: Set<TournamentStatus>,
        nextStatus: TournamentStatus,
        validator: (TournamentEntity) -> Unit = {},
    ): TournamentResponse {
        val tournament = getEntity(id)
        authorizationService.assertCanManageTournament(tournament)
        if (tournament.status !in allowedCurrentStatuses) {
            throw BadRequestException("Tournament cannot transition from ${tournament.status} to $nextStatus")
        }
        validator(tournament)
        tournament.status = nextStatus
        val saved = tournamentRepository.save(tournament)
        auditService.log(
            action = AuditAction.TOURNAMENT_STATUS_CHANGED,
            entityType = AuditEntityType.TOURNAMENT,
            entityId = saved.id,
            tournamentId = saved.id,
            details = mapOf("status" to nextStatus.name),
        )
        realtimeEventPublisher.publishTournamentUpdated(saved.id, "Tournament status changed", mapOf("status" to nextStatus.name))
        return TournamentMapper.toResponse(saved)
    }

    private fun validateDateWindow(startAt: java.time.Instant?, endAt: java.time.Instant?) {
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw BadRequestException("Tournament endAt cannot be before startAt")
        }
    }
}
