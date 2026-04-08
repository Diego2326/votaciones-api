package com.votaciones.api.user.domain

import com.votaciones.api.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "roles",
    indexes = [
        Index(name = "uk_roles_name", columnList = "name", unique = true),
    ],
)
class RoleEntity(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var name: RoleName,
) : BaseEntity()
