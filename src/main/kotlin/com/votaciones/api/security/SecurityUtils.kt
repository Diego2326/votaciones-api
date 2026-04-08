package com.votaciones.api.security

import com.votaciones.api.common.exception.UnauthorizedOperationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SecurityUtils {

    fun currentUser(): UserPrincipal {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return principal as? UserPrincipal ?: throw UnauthorizedOperationException("Authentication required")
    }

    fun currentUserId(): UUID = currentUser().id

    fun hasRole(role: String): Boolean = currentUser().authorities.any { it.authority == "ROLE_$role" }

    fun hasAnyRole(vararg roles: String): Boolean = roles.any(::hasRole)
}
