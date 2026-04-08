package com.votaciones.api.user.domain

import com.votaciones.api.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(
    name = "app_users",
    indexes = [
        Index(name = "uk_users_username", columnList = "username", unique = true),
        Index(name = "uk_users_email", columnList = "email", unique = true),
        Index(name = "idx_users_enabled", columnList = "enabled"),
    ],
)
class UserEntity(
    @Column(nullable = false, length = 64)
    var username: String,
    @Column(nullable = false, length = 128)
    var email: String,
    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String,
    @Column(name = "first_name", nullable = false, length = 100)
    var firstName: String,
    @Column(name = "last_name", nullable = false, length = 100)
    var lastName: String,
    @Column(nullable = false)
    var enabled: Boolean = true,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
    )
    var roles: MutableSet<RoleEntity> = mutableSetOf(),
) : BaseEntity()
