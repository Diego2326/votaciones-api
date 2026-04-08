package com.votaciones.api.round.controller

import com.votaciones.api.common.dto.ApiResponse
import com.votaciones.api.round.dto.CreateRoundRequest
import com.votaciones.api.round.service.RoundService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class RoundController(
    private val roundService: RoundService,
) {

    @PostMapping("/tournaments/{tournamentId}/rounds")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun create(
        @PathVariable tournamentId: UUID,
        @Valid @RequestBody request: CreateRoundRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.status(HttpStatus.CREATED).body(
        ApiResponse(message = "Round created", data = roundService.create(tournamentId, request)),
    )

    @GetMapping("/tournaments/{tournamentId}/rounds")
    fun list(@PathVariable tournamentId: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = roundService.listByTournament(tournamentId)),
    )

    @GetMapping("/rounds/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = roundService.getById(id)),
    )

    @PatchMapping("/rounds/{id}/open")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun open(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Round opened", data = roundService.open(id)),
    )

    @PatchMapping("/rounds/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun close(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Round closed", data = roundService.close(id)),
    )

    @PatchMapping("/rounds/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun process(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Round processed", data = roundService.process(id)),
    )

    @PatchMapping("/rounds/{id}/publish-results")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun publishResults(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Round results published", data = roundService.publishResults(id)),
    )
}
