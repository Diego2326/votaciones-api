package com.votaciones.api.access.service

import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64

@Service
class TournamentSessionTokenService {
    private val secureRandom = SecureRandom()

    fun randomPin(length: Int = 6): String = buildString(length) {
        repeat(length) { append(secureRandom.nextInt(10)) }
    }

    fun randomToken(bytes: Int = 32): String {
        val buffer = ByteArray(bytes)
        secureRandom.nextBytes(buffer)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer)
    }
}
