package com.votaciones.api.websocket.dto

enum class WebSocketEventType {
    TOURNAMENT_UPDATED,
    ROUND_OPENED,
    ROUND_CLOSED,
    VOTE_COUNT_UPDATED,
    RESULTS_PUBLISHED,
    PARTICIPATION_UPDATED,
}
