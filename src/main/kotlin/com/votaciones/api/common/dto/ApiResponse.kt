package com.votaciones.api.common.dto

import java.time.Instant

data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null,
    val timestamp: Instant = Instant.now(),
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
)

data class ValidationError(
    val field: String?,
    val message: String,
)

data class ErrorResponse(
    val success: Boolean = false,
    val status: Int,
    val message: String,
    val path: String,
    val errors: List<ValidationError> = emptyList(),
    val timestamp: Instant = Instant.now(),
)
