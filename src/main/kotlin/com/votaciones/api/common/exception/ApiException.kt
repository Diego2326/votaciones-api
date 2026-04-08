package com.votaciones.api.common.exception

import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    override val message: String,
) : RuntimeException(message)

class NotFoundException(message: String) : ApiException(HttpStatus.NOT_FOUND, message)

class ConflictException(message: String) : ApiException(HttpStatus.CONFLICT, message)

class BadRequestException(message: String) : ApiException(HttpStatus.BAD_REQUEST, message)

class ForbiddenOperationException(message: String) : ApiException(HttpStatus.FORBIDDEN, message)

class UnauthorizedOperationException(message: String) : ApiException(HttpStatus.UNAUTHORIZED, message)
