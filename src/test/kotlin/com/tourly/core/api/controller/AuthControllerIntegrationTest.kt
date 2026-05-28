package com.tourly.core.api.controller

import com.ninjasquad.springmockk.MockkBean
import com.tourly.core.api.dto.auth.LoginRequestDto
import com.tourly.core.api.dto.auth.LoginResponseDto
import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.service.AuthService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tourly.core.security.JWTUtil
import com.tourly.core.service.CustomUserDetailsService
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @MockkBean
    private lateinit var authService: AuthService

    @MockkBean
    private lateinit var jwtUtil: JWTUtil

    @MockkBean
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @Test
    fun `login should return 200 OK and token when valid request`() {
        // Arrange
        val request = LoginRequestDto(email = "test@example.com", password = "password")
        val response = LoginResponseDto(
            token = "jwt.token.here",
            user = UserDto(
                id = 1,
                email = "test@example.com",
                firstName = "Test",
                lastName = "User",
                role = UserRole.TRAVELER,
                profilePictureUrl = null
            )
        )

        every { authService.login(any()) } returns response

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt.token.here"))
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
    }

    @Test
    fun `login should return 400 Bad Request when request is invalid`() {
        // Arrange - empty email violates @Valid
        val request = LoginRequestDto(email = "", password = "")

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }
}
