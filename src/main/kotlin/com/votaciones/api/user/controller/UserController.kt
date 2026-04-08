package com.votaciones.api.user.controller

import com.votaciones.api.common.dto.ApiResponse
import com.votaciones.api.user.dto.UpdateUserRolesRequest
import com.votaciones.api.user.dto.UpdateUserStatusRequest
import com.votaciones.api.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
class UserController(
    private val userService: UserService,
) {

    @GetMapping
    fun listUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = userService.listUsers(page, size)),
    )

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(data = userService.getUser(id)),
    )

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserStatusRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "User status updated", data = userService.updateStatus(id, request)),
    )

    @PatchMapping("/{id}/roles")
    fun updateRoles(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRolesRequest,
    ): ResponseEntity<ApiResponse<Any>> = ResponseEntity.ok(
        ApiResponse(message = "User roles updated", data = userService.updateRoles(id, request)),
    )
}
