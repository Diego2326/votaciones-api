package com.votaciones.api.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.votaciones.api.common.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class RestAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(
            objectMapper.writeValueAsString(
                ErrorResponse(
                    status = HttpServletResponse.SC_FORBIDDEN,
                    message = accessDeniedException.message ?: "Access denied",
                    path = request.requestURI,
                ),
            ),
        )
    }
}
