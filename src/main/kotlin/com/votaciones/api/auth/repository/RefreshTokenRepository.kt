package com.votaciones.api.auth.repository

import com.votaciones.api.auth.domain.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHashAndRevokedFalse(tokenHash: String): RefreshTokenEntity?
    fun findAllByUserIdAndRevokedFalse(userId: UUID): List<RefreshTokenEntity>
}
