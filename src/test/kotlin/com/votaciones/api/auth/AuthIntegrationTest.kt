package com.votaciones.api.auth

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.votaciones.api.access.domain.TournamentAccessMode
import com.votaciones.api.auth.dto.LoginRequest
import com.votaciones.api.auth.dto.RegisterRequest
import com.votaciones.api.tournament.domain.TournamentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest(
    @Autowired private val objectMapper: ObjectMapper,
    @LocalServerPort private val port: Int,
) {

    @Test
    fun `register endpoint exposes get contract`() {
        val response = client()
            .get()
            .uri("/api/v1/auth/register")
            .retrieve()
            .toEntity(String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = readJson(response.body!!)
        assertEquals("Send a POST request to register a user", body.path("message").asText())
        assertEquals("POST", body.path("data").path("method").asText())
        assertEquals("/api/v1/auth/register", body.path("data").path("path").asText())
    }

    @Test
    fun `register then access me endpoint`() {
        val registerResponse = post(
            "/api/v1/auth/register",
            RegisterRequest(
                username = "organizer_auth_test",
                email = "organizer_auth_test@example.com",
                password = "Password123!",
                firstName = "Organizer",
                lastName = "Tester",
            ),
        )

        assertEquals(HttpStatus.CREATED, registerResponse.statusCode)
        val registerBody = readJson(registerResponse.body!!)
        val accessToken = registerBody.path("data").path("tokens").path("accessToken").asText()
        assertTrue(accessToken.isNotBlank())
        assertTrue(registerBody.path("data").path("user").path("roles").toString().contains("ORGANIZER"))

        val meResponse = client()
            .get()
            .uri("/api/v1/auth/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .retrieve()
            .toEntity(String::class.java)

        assertEquals(HttpStatus.OK, meResponse.statusCode)
        assertEquals("organizer_auth_test", readJson(meResponse.body!!).path("data").path("username").asText())
    }

    @Test
    fun `registered user can create tournament`() {
        val registerResponse = post(
            "/api/v1/auth/register",
            RegisterRequest(
                username = "organizer_tournament_test",
                email = "organizer_tournament_test@example.com",
                password = "Password123!",
                firstName = "Tournament",
                lastName = "Owner",
            ),
        )

        assertEquals(HttpStatus.CREATED, registerResponse.statusCode)
        val accessToken = readJson(registerResponse.body!!).path("data").path("tokens").path("accessToken").asText()

        val createResponse = client()
            .post()
            .uri("/api/v1/tournaments")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                mapOf(
                    "title" to "Organizer Tournament",
                    "description" to "Created from register",
                    "type" to TournamentType.POLL.name,
                    "accessMode" to TournamentAccessMode.DISPLAY_NAME.name,
                ),
            )
            .retrieve()
            .toEntity(String::class.java)

        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        assertEquals("Organizer Tournament", readJson(createResponse.body!!).path("data").path("title").asText())
    }

    @Test
    fun `protected tournament creation requires authentication`() {
        val response = client()
            .post()
            .uri("/api/v1/tournaments")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                mapOf(
                    "title" to "Unauthorized Tournament",
                    "type" to "POLL",
                ),
            )
            .exchange { _, httpResponse -> httpResponse.statusCode }

        assertEquals(HttpStatus.UNAUTHORIZED, response)
    }

    @Test
    fun `login returns tokens for registered user`() {
        post(
            "/api/v1/auth/register",
            RegisterRequest(
                username = "login_test_user",
                email = "login_test_user@example.com",
                password = "Password123!",
                firstName = "Login",
                lastName = "Tester",
            ),
        )

        val loginResponse = post(
            "/api/v1/auth/login",
            LoginRequest(
                usernameOrEmail = "login_test_user",
                password = "Password123!",
            ),
        )

        assertEquals(HttpStatus.OK, loginResponse.statusCode)
        val body = readJson(loginResponse.body!!)
        assertFalse(body.path("data").path("tokens").path("refreshToken").asText().isBlank())
        assertNotNull(body.path("data").path("user").path("id").asText())
    }

    private fun post(path: String, body: Any) = client()
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .toEntity(String::class.java)

    private fun client(): RestClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .build()

    private fun readJson(content: String): JsonNode = objectMapper.readTree(content)
}
