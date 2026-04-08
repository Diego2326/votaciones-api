package com.votaciones.api.websocket.service

import com.votaciones.api.websocket.dto.RealtimeEventResponse
import com.votaciones.api.websocket.dto.WebSocketEventType
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RealtimeEventPublisher(
    private val messagingTemplate: SimpMessagingTemplate,
) {

    fun publishTournamentUpdated(
        tournamentId: UUID,
        message: String,
        payload: Map<String, Any?> = emptyMap(),
    ) = publish(
        destination = "/topic/tournament/$tournamentId",
        event = RealtimeEventResponse(
            eventType = WebSocketEventType.TOURNAMENT_UPDATED,
            tournamentId = tournamentId,
            message = message,
            payload = payload,
        ),
    )

    fun publishRoundOpened(tournamentId: UUID, roundId: UUID) = publishToRound(
        tournamentId = tournamentId,
        roundId = roundId,
        eventType = WebSocketEventType.ROUND_OPENED,
        message = "Round opened",
    )

    fun publishRoundClosed(tournamentId: UUID, roundId: UUID) = publishToRound(
        tournamentId = tournamentId,
        roundId = roundId,
        eventType = WebSocketEventType.ROUND_CLOSED,
        message = "Round closed",
    )

    fun publishVoteCountUpdated(
        tournamentId: UUID,
        roundId: UUID,
        matchId: UUID,
        totalVotes: Long,
    ) = publishToRound(
        tournamentId = tournamentId,
        roundId = roundId,
        eventType = WebSocketEventType.VOTE_COUNT_UPDATED,
        message = "Vote count updated",
        matchId = matchId,
        payload = mapOf("totalVotes" to totalVotes),
    )

    fun publishResultsPublished(tournamentId: UUID, roundId: UUID) = publishToRound(
        tournamentId = tournamentId,
        roundId = roundId,
        eventType = WebSocketEventType.RESULTS_PUBLISHED,
        message = "Results published",
    )

    fun publishParticipationUpdated(tournamentId: UUID, participantId: UUID, action: String) = publish(
        destination = "/topic/tournament/$tournamentId",
        event = RealtimeEventResponse(
            eventType = WebSocketEventType.PARTICIPATION_UPDATED,
            tournamentId = tournamentId,
            message = "Participant $action",
            payload = mapOf("participantId" to participantId, "action" to action),
        ),
    )

    private fun publishToRound(
        tournamentId: UUID,
        roundId: UUID,
        eventType: WebSocketEventType,
        message: String,
        matchId: UUID? = null,
        payload: Map<String, Any?> = emptyMap(),
    ) {
        publish(
            destination = "/topic/tournament/$tournamentId/round/$roundId",
            event = RealtimeEventResponse(
                eventType = eventType,
                tournamentId = tournamentId,
                roundId = roundId,
                matchId = matchId,
                message = message,
                payload = payload,
            ),
        )
    }

    private fun publish(destination: String, event: RealtimeEventResponse) {
        messagingTemplate.convertAndSend(destination, event)
    }
}
