package com.votaciones.api.auth.dto

import com.votaciones.api.user.dto.UserResponse
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 64)
    val username: String,
    @field:NotBlank
    @field:Email
    @field:Size(max = 128)
    val email: String,
    @field:NotBlank
    @field:Size(min = 8, max = 72)
    val password: String,
    @field:NotBlank
    @field:Size(max = 100)
    val firstName: String,
    @field:NotBlank
    @field:Size(max = 100)
    val lastName: String,
)

data class LoginRequest(
    @field:NotBlank
    val usernameOrEmail: String,
    @field:NotBlank
    val password: String,
)

data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
)

data class AuthResponse(
    val tokens: TokenResponse,
    val user: UserResponse,
)
