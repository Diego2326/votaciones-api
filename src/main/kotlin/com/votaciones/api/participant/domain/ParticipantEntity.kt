package com.votaciones.api.participant.domain

import com.votaciones.api.common.entity.BaseEntity
import com.votaciones.api.tournament.domain.TournamentEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "participants",
    indexes = [
        Index(name = "idx_participants_tournament", columnList = "tournament_id"),
        Index(name = "idx_participants_active", columnList = "active"),
    ],
)
class ParticipantEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    var tournament: TournamentEntity,
    @Column(nullable = false, length = 160)
    var name: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,
    @Column(nullable = false)
    var active: Boolean = true,
) : BaseEntity()
