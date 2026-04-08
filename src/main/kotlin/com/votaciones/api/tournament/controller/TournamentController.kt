package com.votaciones.api.tournament.controller

import com.votaciones.api.common.dto.ApiResponse
import com.votaciones.api.tournament.domain.TournamentStatus
import com.votaciones.api.tournament.dto.CreateTournamentRequest
import com.votaciones.api.tournament.dto.UpdateTournamentRequest
import com.votaciones.api.tournament.service.TournamentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/tournaments")
class TournamentController(
    private val tournamentService: TournamentService,
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun create(@Valid @RequestBody request: CreateTournamentRequest): ResponseEntity<ApiResponse<Any>> = ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse(message = "Tournament created", data = tournamentService.create(request)))

    @GetMapping
    fun list(
        @RequestParam(required = false) status: TournamentStatus?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = tournamentService.list(status, page, size)),
    )

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = tournamentService.getById(id)),
    )

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateTournamentRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament updated", data = tournamentService.update(id, request)),
    )

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun publish(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament published", data = tournamentService.publish(id)),
    )

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun activate(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament activated", data = tournamentService.activate(id)),
    )

    @PatchMapping("/{id}/pause")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun pause(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament paused", data = tournamentService.pause(id)),
    )

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun close(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament closed", data = tournamentService.close(id)),
    )
}
