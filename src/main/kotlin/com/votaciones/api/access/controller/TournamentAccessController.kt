package com.votaciones.api.access.controller

import com.votaciones.api.access.dto.JoinByDisplayNameRequest
import com.votaciones.api.access.dto.JoinByEmailPasswordRequest
import com.votaciones.api.access.dto.JoinByPinRequest
import com.votaciones.api.access.dto.JoinByQrRequest
import com.votaciones.api.access.dto.UpdateTournamentAccessRequest
import com.votaciones.api.access.service.TournamentAccessService
import com.votaciones.api.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class TournamentAccessController(
    private val tournamentAccessService: TournamentAccessService,
) {

    @GetMapping("/tournaments/{tournamentId}/access")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun getAccess(@PathVariable tournamentId: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = tournamentAccessService.getTournamentAccess(tournamentId)),
    )

    @PatchMapping("/tournaments/{tournamentId}/access")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun updateAccess(
        @PathVariable tournamentId: UUID,
        @Valid @RequestBody request: UpdateTournamentAccessRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament access updated", data = tournamentAccessService.updateTournamentAccess(tournamentId, request)),
    )

    @PatchMapping("/tournaments/{tournamentId}/regenerate-pin")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun regeneratePin(@PathVariable tournamentId: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament pin regenerated", data = tournamentAccessService.regeneratePin(tournamentId)),
    )

    @PostMapping("/join/pin")
    fun joinInfo(@Valid @RequestBody request: JoinByPinRequest): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = tournamentAccessService.resolveTournamentByPin(request)),
    )

    @PostMapping("/join/qr/info")
    fun joinQrInfo(@Valid @RequestBody request: JoinByQrRequest): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = tournamentAccessService.resolveTournamentByQr(request)),
    )

    @PostMapping("/join/name")
    fun joinByName(@Valid @RequestBody request: JoinByDisplayNameRequest): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament session created", data = tournamentAccessService.joinByDisplayName(request)),
    )

    @PostMapping("/join/qr")
    fun joinByQr(@Valid @RequestBody request: JoinByQrRequest): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament session created", data = tournamentAccessService.joinAnonymously(request)),
    )

    @PostMapping("/join/auth")
    fun joinByAuth(@Valid @RequestBody request: JoinByEmailPasswordRequest): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Tournament session created", data = tournamentAccessService.joinByEmailPassword(request)),
    )

    @GetMapping("/join/me")
    fun sessionMe(@RequestHeader("X-Tournament-Session") sessionToken: String): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = tournamentAccessService.touchSession(sessionToken)),
    )
}
