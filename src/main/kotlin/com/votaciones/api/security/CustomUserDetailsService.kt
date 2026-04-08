package com.votaciones.api.security

import com.votaciones.api.common.exception.NotFoundException
import com.votaciones.api.user.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found") }

        return UserPrincipal.from(user)
    }

    fun loadPrincipalByUserId(username: String): UserPrincipal = loadUserByUsername(username) as? UserPrincipal
        ?: throw NotFoundException("User not found")
}
