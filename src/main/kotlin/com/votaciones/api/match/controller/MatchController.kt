package com.votaciones.api.match.controller

import com.votaciones.api.common.dto.ApiResponse
import com.votaciones.api.match.dto.CreateMatchesRequest
import com.votaciones.api.match.dto.RegisterWinnerRequest
import com.votaciones.api.match.service.MatchService
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
class MatchController(
    private val matchService: MatchService,
) {

    @PostMapping("/rounds/{roundId}/matches")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun create(
        @PathVariable roundId: UUID,
        @Valid @RequestBody request: CreateMatchesRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.status(HttpStatus.CREATED).body(
        ApiResponse(message = "Matches created", data = matchService.create(roundId, request)),
    )

    @GetMapping("/rounds/{roundId}/matches")
    fun list(@PathVariable roundId: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = matchService.listByRound(roundId)),
    )

    @GetMapping("/matches/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = matchService.getById(id)),
    )

    @PatchMapping("/matches/{id}/winner")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun registerWinner(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RegisterWinnerRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Winner registered", data = matchService.registerWinner(id, request)),
    )
}
