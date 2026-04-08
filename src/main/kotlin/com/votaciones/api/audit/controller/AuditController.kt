package com.votaciones.api.audit.controller

import com.votaciones.api.audit.service.AuditService
import com.votaciones.api.common.dto.ApiResponse
import com.votaciones.api.security.AuthorizationService
import com.votaciones.api.tournament.service.TournamentService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class AuditController(
    private val auditService: AuditService,
    private val tournamentService: TournamentService,
    private val authorizationService: AuthorizationService,
) {

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    fun listAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = auditService.listAll(page, size)),
    )

    @GetMapping("/tournaments/{tournamentId}/audit")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    fun listByTournament(
        @PathVariable tournamentId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ApiResponse<Any>> {
        authorizationService.assertCanManageTournament(tournamentService.getEntity(tournamentId))
        return ResponseEntity.ok(ApiResponse(data = auditService.listByTournament(tournamentId, page, size)))
    }
}
