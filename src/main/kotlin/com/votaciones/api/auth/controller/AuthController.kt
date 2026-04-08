package com.votaciones.api.auth.controller

import com.votaciones.api.auth.dto.LoginRequest
import com.votaciones.api.auth.dto.RefreshTokenRequest
import com.votaciones.api.auth.dto.RegisterRequest
import com.votaciones.api.auth.service.AuthService
import com.votaciones.api.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @GetMapping("/register")
    fun registerInfo(): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(
            message = "Send a POST request to register a user",
            data = mapOf(
                "method" to "POST",
                "path" to "/api/v1/auth/register",
                "requiredFields" to listOf("username", "email", "password", "firstName", "lastName"),
            ),
        ),
    )

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<Any>> = ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse(message = "User registered successfully", data = authService.register(request)))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Login successful", data = authService.login(request)),
    )

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "Token refreshed successfully", data = authService.refresh(request)),
    )

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun me(): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = authService.me()),
    )
}
