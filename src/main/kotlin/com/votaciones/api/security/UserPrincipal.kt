package com.votaciones.api.security

import com.votaciones.api.user.domain.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

data class UserPrincipal(
    val id: UUID,
    private val usernameValue: String,
    val email: String,
    private val passwordHash: String,
    private val enabledValue: Boolean,
    private val authoritiesValue: Collection<GrantedAuthority>,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authoritiesValue

    override fun getPassword(): String = passwordHash

    override fun getUsername(): String = usernameValue

    override fun isEnabled(): Boolean = enabledValue

    companion object {
        fun from(entity: UserEntity): UserPrincipal = UserPrincipal(
            id = entity.id,
            usernameValue = entity.username,
            email = entity.email,
            passwordHash = entity.passwordHash,
            enabledValue = entity.enabled,
            authoritiesValue = entity.roles.map { SimpleGrantedAuthority("ROLE_${it.name.name}") },
        )
    }
}
