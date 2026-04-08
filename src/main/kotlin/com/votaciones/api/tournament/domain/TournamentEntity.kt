package com.votaciones.api.tournament.domain

import com.votaciones.api.common.entity.BaseEntity
import com.votaciones.api.user.domain.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "tournaments",
    indexes = [
        Index(name = "idx_tournaments_status", columnList = "status"),
        Index(name = "idx_tournaments_type", columnList = "type"),
        Index(name = "idx_tournaments_created_by", columnList = "created_by"),
    ],
)
class TournamentEntity(
    @Column(nullable = false, length = 160)
    var title: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var type: TournamentType,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: TournamentStatus = TournamentStatus.DRAFT,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: UserEntity,
    @Column(name = "start_at")
    var startAt: Instant? = null,
    @Column(name = "end_at")
    var endAt: Instant? = null,
) : BaseEntity()
