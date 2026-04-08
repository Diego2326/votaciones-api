package com.votaciones.api.common.entity

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.Instant
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity(
    @Id
    @Column(nullable = false, updatable = false)
    open var id: UUID = UUID.randomUUID(),
    @Column(nullable = false, updatable = false)
    open var createdAt: Instant = Instant.now(),
    @Column(nullable = false)
    open var updatedAt: Instant = Instant.now(),
) {
    @PrePersist
    fun onCreate() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}
