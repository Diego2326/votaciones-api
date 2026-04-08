package com.votaciones.api.auth.domain

import com.votaciones.api.common.entity.BaseEntity
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
    name = "refresh_tokens",
    indexes = [
        Index(name = "uk_refresh_tokens_hash", columnList = "token_hash", unique = true),
        Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
        Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at"),
    ],
)
class RefreshTokenEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @Column(name = "token_hash", nullable = false, length = 128)
    var tokenHash: String,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
    @Column(nullable = false)
    var revoked: Boolean = false,
    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,
) : BaseEntity()
