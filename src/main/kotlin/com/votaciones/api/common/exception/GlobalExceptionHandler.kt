package com.votaciones.api.common.exception

import com.votaciones.api.common.dto.ErrorResponse
import com.votaciones.api.common.dto.ValidationError
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        exception: ApiException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(
        status = exception.status,
        message = exception.message,
        path = request.requestURI,
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(
        status = HttpStatus.BAD_REQUEST,
        message = "Validation failed",
        path = request.requestURI,
        errors = exception.bindingResult.allErrors.map { error ->
            val fieldName = (error as? FieldError)?.field
            ValidationError(fieldName, error.defaultMessage ?: "Invalid value")
        },
    )

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        exception: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(
        status = HttpStatus.BAD_REQUEST,
        message = "Validation failed",
        path = request.requestURI,
        errors = exception.constraintViolations.map { violation ->
            ValidationError(violation.propertyPath.toString(), violation.message)
        },
    )

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        exception: AuthenticationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(
        status = HttpStatus.UNAUTHORIZED,
        message = exception.message ?: "Authentication failed",
        path = request.requestURI,
    )

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        exception: AccessDeniedException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(
        status = HttpStatus.FORBIDDEN,
        message = exception.message ?: "Access denied",
        path = request.requestURI,
    )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(
        status = HttpStatus.BAD_REQUEST,
        message = exception.message ?: "Malformed request body",
        path = request.requestURI,
    )

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        message = exception.message ?: "Unexpected server error",
        path = request.requestURI,
    )

    private fun buildResponse(
        status: HttpStatus,
        message: String,
        path: String,
        errors: List<ValidationError> = emptyList(),
    ): ResponseEntity<ErrorResponse> = ResponseEntity.status(status).body(
        ErrorResponse(
            status = status.value(),
            message = message,
            path = path,
            errors = errors,
        ),
    )
}
