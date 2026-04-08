package com.votaciones.api.user.repository

import com.votaciones.api.user.domain.RoleEntity
import com.votaciones.api.user.domain.RoleName
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RoleRepository : JpaRepository<RoleEntity, UUID> {
    fun findByName(name: RoleName): RoleEntity?
    fun findAllByNameIn(names: Set<RoleName>): List<RoleEntity>
}
