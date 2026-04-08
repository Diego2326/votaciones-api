package com.votaciones.api.websocket.dto

import java.time.Instant
import java.util.UUID

data class RealtimeEventResponse(
    val eventType: WebSocketEventType,
    val tournamentId: UUID,
    val roundId: UUID? = null,
    val matchId: UUID? = null,
    val message: String,
    val payload: Map<String, Any?> = emptyMap(),
    val emittedAt: Instant = Instant.now(),
)
