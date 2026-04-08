package com.votaciones.api.match.domain

import com.votaciones.api.common.entity.BaseEntity
import com.votaciones.api.participant.domain.ParticipantEntity
import com.votaciones.api.round.domain.RoundEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "matches",
    indexes = [
        Index(name = "idx_matches_round", columnList = "round_id"),
        Index(name = "idx_matches_status", columnList = "status"),
    ],
)
class MatchEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    var round: RoundEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_a_id", nullable = false)
    var participantA: ParticipantEntity,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_b_id", nullable = false)
    var participantB: ParticipantEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    var winner: ParticipantEntity? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: MatchStatus = MatchStatus.PENDING,
) : BaseEntity()
