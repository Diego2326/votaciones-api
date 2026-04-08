package com.votaciones.api.participant.service

import com.votaciones.api.audit.domain.AuditAction
import com.votaciones.api.audit.domain.AuditEntityType
import com.votaciones.api.audit.service.AuditService
import com.votaciones.api.common.exception.NotFoundException
import com.votaciones.api.participant.domain.ParticipantEntity
import com.votaciones.api.participant.dto.CreateParticipantRequest
import com.votaciones.api.participant.dto.ParticipantResponse
import com.votaciones.api.participant.dto.UpdateParticipantRequest
import com.votaciones.api.participant.mapper.ParticipantMapper
import com.votaciones.api.participant.repository.ParticipantRepository
import com.votaciones.api.security.AuthorizationService
import com.votaciones.api.tournament.service.TournamentService
import com.votaciones.api.websocket.service.RealtimeEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val tournamentService: TournamentService,
    private val authorizationService: AuthorizationService,
    private val auditService: AuditService,
    private val realtimeEventPublisher: RealtimeEventPublisher,
) {

    @Transactional
    fun create(tournamentId: UUID, request: CreateParticipantRequest): ParticipantResponse {
        val tournament = tournamentService.getEntity(tournamentId)
        authorizationService.assertCanManageTournament(tournament)
        tournamentService.assertEditable(tournament)

        val participant = participantRepository.save(
            ParticipantEntity(
                tournament = tournament,
                name = request.name.trim(),
                description = request.description?.trim(),
                imageUrl = request.imageUrl?.trim(),
                active = request.active,
            ),
        )
        auditService.log(
            action = AuditAction.PARTICIPANT_CREATED,
            entityType = AuditEntityType.PARTICIPANT,
            entityId = participant.id,
            tournamentId = tournament.id,
            details = mapOf("name" to participant.name),
        )
        realtimeEventPublisher.publishParticipationUpdated(tournament.id, participant.id, "created")
        return ParticipantMapper.toResponse(participant)
    }

    @Transactional(readOnly = true)
    fun listByTournament(tournamentId: UUID): List<ParticipantResponse> = participantRepository
        .findAllByTournamentIdOrderByCreatedAtAsc(tournamentId)
        .map(ParticipantMapper::toResponse)

    @Transactional
    fun update(id: UUID, request: UpdateParticipantRequest): ParticipantResponse {
        val participant = getEntity(id)
        authorizationService.assertCanManageTournament(participant.tournament)
        tournamentService.assertEditable(participant.tournament)

        participant.name = request.name.trim()
        participant.description = request.description?.trim()
        participant.imageUrl = request.imageUrl?.trim()
        participant.active = request.active

        val saved = participantRepository.save(participant)
        auditService.log(
            action = AuditAction.PARTICIPANT_UPDATED,
            entityType = AuditEntityType.PARTICIPANT,
            entityId = saved.id,
            tournamentId = saved.tournament.id,
            details = mapOf("name" to saved.name, "active" to saved.active),
        )
        realtimeEventPublisher.publishParticipationUpdated(saved.tournament.id, saved.id, "updated")
        return ParticipantMapper.toResponse(saved)
    }

    @Transactional
    fun delete(id: UUID) {
        val participant = getEntity(id)
        authorizationService.assertCanManageTournament(participant.tournament)
        tournamentService.assertEditable(participant.tournament)

        participant.active = false
        participantRepository.save(participant)
        auditService.log(
            action = AuditAction.PARTICIPANT_DELETED,
            entityType = AuditEntityType.PARTICIPANT,
            entityId = participant.id,
            tournamentId = participant.tournament.id,
            details = mapOf("softDeleted" to true),
        )
        realtimeEventPublisher.publishParticipationUpdated(participant.tournament.id, participant.id, "deleted")
    }

    @Transactional(readOnly = true)
    fun getEntity(id: UUID): ParticipantEntity = participantRepository.findById(id)
        .orElseThrow { NotFoundException("Participant $id not found") }
}
