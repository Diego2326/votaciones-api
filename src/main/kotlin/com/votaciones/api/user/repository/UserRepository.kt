package com.votaciones.api.user.repository

import com.votaciones.api.user.domain.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByUsername(username: String): Optional<UserEntity>
    fun findByEmail(email: String): Optional<UserEntity>
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
