package com.votaciones.api.round.domain

import com.votaciones.api.common.entity.BaseEntity
import com.votaciones.api.tournament.domain.TournamentEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "tournament_rounds",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_round_tournament_number", columnNames = ["tournament_id", "round_number"]),
    ],
    indexes = [
        Index(name = "idx_rounds_tournament", columnList = "tournament_id"),
        Index(name = "idx_rounds_status", columnList = "status"),
    ],
)
class RoundEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    var tournament: TournamentEntity,
    @Column(nullable = false, length = 120)
    var name: String,
    @Column(name = "round_number", nullable = false)
    var roundNumber: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: RoundStatus = RoundStatus.PENDING,
    @Column(name = "opens_at")
    var opensAt: Instant? = null,
    @Column(name = "closes_at")
    var closesAt: Instant? = null,
    @Column(name = "results_published_at")
    var resultsPublishedAt: Instant? = null,
) : BaseEntity()
