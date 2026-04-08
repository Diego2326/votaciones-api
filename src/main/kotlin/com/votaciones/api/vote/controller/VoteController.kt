package com.votaciones.api.vote.controller

import com.votaciones.api.common.dto.ApiResponse
import com.votaciones.api.vote.dto.CastVoteRequest
import com.votaciones.api.vote.service.VoteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class VoteController(
    private val voteService: VoteService,
) {

    @PostMapping("/matches/{matchId}/vote")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER','VOTER')")
    fun vote(
        @PathVariable matchId: UUID,
        @Valid @RequestBody request: CastVoteRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.status(HttpStatus.CREATED).body(
        ApiResponse(message = "Vote recorded", data = voteService.castVote(matchId, request)),
    )

    @GetMapping("/matches/{matchId}/results")
    fun matchResults(@PathVariable matchId: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = voteService.getMatchResults(matchId)),
    )

    @GetMapping("/rounds/{roundId}/results")
    fun roundResults(@PathVariable roundId: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = voteService.getRoundResults(roundId)),
    )

    @GetMapping("/tournaments/{tournamentId}/results")
    fun tournamentResults(@PathVariable tournamentId: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = voteService.getTournamentResults(tournamentId)),
    )

    @GetMapping("/matches/{matchId}/my-vote")
    @PreAuthorize("isAuthenticated()")
    fun myVote(@PathVariable matchId: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = voteService.getMyVote(matchId)),
    )
}
