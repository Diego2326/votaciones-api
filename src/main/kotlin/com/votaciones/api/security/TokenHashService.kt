package com.votaciones.api.security

import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class TokenHashService {

    fun hash(rawValue: String): String = MessageDigest.getInstance("SHA-256")
        .digest(rawValue.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
