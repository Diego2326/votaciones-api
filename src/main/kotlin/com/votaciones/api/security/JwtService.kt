package com.votaciones.api.security

import com.votaciones.api.common.exception.UnauthorizedOperationException
import com.votaciones.api.user.domain.UserEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    private val jwtProperties: JwtProperties,
) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))

    fun generateAccessToken(user: UserEntity): String = buildToken(
        user = user,
        type = "access",
        ttlSeconds = jwtProperties.accessTokenTtl.seconds,
    )

    fun generateRefreshToken(user: UserEntity): String = buildToken(
        user = user,
        type = "refresh",
        ttlSeconds = jwtProperties.refreshTokenTtl.seconds,
    )

    fun extractUsername(token: String): String = parseClaims(token).subject

    fun extractUserId(token: String): UUID = UUID.fromString(parseClaims(token)["uid"].toString())

    fun extractType(token: String): String = parseClaims(token)["type"].toString()

    fun isTokenValid(token: String, principal: UserPrincipal): Boolean = try {
        val claims = parseClaims(token)
        claims.subject == principal.username &&
            UUID.fromString(claims["uid"].toString()) == principal.id &&
            claims.expiration.after(Date.from(Instant.now()))
    } catch (_: Exception) {
        false
    }

    fun validateRefreshToken(token: String): UUID {
        val claims = parseClaims(token)
        if (claims["type"] != "refresh") {
            throw UnauthorizedOperationException("Invalid refresh token type")
        }
        return UUID.fromString(claims["uid"].toString())
    }

    private fun buildToken(
        user: UserEntity,
        type: String,
        ttlSeconds: Long,
    ): String {
        val now = Instant.now()
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .issuer(jwtProperties.issuer)
            .subject(user.username)
            .claim("uid", user.id.toString())
            .claim("email", user.email)
            .claim("roles", user.roles.map { it.name.name })
            .claim("type", type)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(ttlSeconds)))
            .signWith(signingKey)
            .compact()
    }

    private fun parseClaims(token: String): Claims = try {
        Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(jwtProperties.issuer)
            .build()
            .parseSignedClaims(token)
            .payload
    } catch (exception: Exception) {
        throw UnauthorizedOperationException("Invalid or expired token")
    }
}
