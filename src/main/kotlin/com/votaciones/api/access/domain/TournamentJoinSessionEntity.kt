package com.votaciones.api.access.domain

import com.votaciones.api.common.entity.BaseEntity
import com.votaciones.api.tournament.domain.TournamentEntity
import com.votaciones.api.user.domain.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "tournament_join_sessions",
    indexes = [
        Index(name = "uk_join_sessions_token_hash", columnList = "session_token_hash", unique = true),
        Index(name = "idx_join_sessions_tournament", columnList = "tournament_id"),
        Index(name = "idx_join_sessions_user", columnList = "user_id"),
        Index(name = "idx_join_sessions_active", columnList = "active"),
    ],
)
class TournamentJoinSessionEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    var tournament: TournamentEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null,
    @Column(name = "display_name", length = 120)
    var displayName: String? = null,
    @Column(name = "session_token_hash", nullable = false, length = 128)
    var sessionTokenHash: String,
    @Column(nullable = false)
    var active: Boolean = true,
    @Column(name = "joined_at", nullable = false)
    var joinedAt: Instant = Instant.now(),
    @Column(name = "last_seen_at", nullable = false)
    var lastSeenAt: Instant = Instant.now(),
    @Column(name = "expires_at")
    var expiresAt: Instant? = null,
) : BaseEntity()
