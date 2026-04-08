package com.votaciones.api.participant.controller

import com.votaciones.api.common.dto.ApiResponse
import com.votaciones.api.participant.dto.CreateParticipantRequest
import com.votaciones.api.participant.dto.UpdateParticipantRequest
import com.votaciones.api.participant.service.ParticipantService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class ParticipantController(
    private val participantService: ParticipantService,
) {

    @PostMapping("/tournaments/{tournamentId}/participants")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun create(
        @PathVariable tournamentId: UUID,
        @Valid @RequestBody request: CreateParticipantRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.status(HttpStatus.CREATED).body(
        ApiResponse(message = "Participant created", data = participantService.create(tournamentId, request)),
    )

    @GetMapping("/tournaments/{tournamentId}/participants")
    fun list(@PathVariable tournamentId: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = participantService.listByTournament(tournamentId)),
    )

    @PutMapping("/participants/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateParticipantRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Participant updated", data = participantService.update(id, request)),
    )

    @DeleteMapping("/participants/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        participantService.delete(id)
        return ResponseEntity.ok(ApiResponse(message = "Participant deleted"))
    }
}
