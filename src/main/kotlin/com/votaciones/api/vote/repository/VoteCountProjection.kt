package com.votaciones.api.vote.repository

import java.util.UUID

interface VoteCountProjection {
    val participantId: UUID
    val votes: Long
}
