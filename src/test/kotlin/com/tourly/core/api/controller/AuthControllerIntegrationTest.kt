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
import com.tourly.core.api.dto.auth.RefreshTokenRequestDto
import com.tourly.core.api.dto.auth.RefreshTokenResponseDto
import com.tourly.core.api.dto.auth.RegisterRequestDto
import com.tourly.core.api.dto.auth.RegisterResponseDto
import com.tourly.core.security.JWTUtil
import com.tourly.core.service.CustomUserDetailsService
import io.mockk.justRun
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

    //  shared test data
    private val userDto = UserDto(
        id = 1L,
        email = "test@example.com",
        firstName = "Test",
        lastName = "User",
        role = UserRole.TRAVELER,
        profilePictureUrl = null
    )

    // -- /login -------------

    @Test
    fun `login should return 200 and token when valid request`() {
        val request = LoginRequestDto(email = "test@example.com", password = "password123")
        val response = LoginResponseDto(
            token = "jwt.token.here",
            refreshToken = "jwt.refresh.token.here",
            user = userDto
        )

        every { authService.login(any()) } returns response

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt.token.here"))
            .andExpect(jsonPath("$.refreshToken").value("jwt.refresh.token.here"))
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
    }

    @Test
    fun `login should return 400 when email is blank`() {
        val request = LoginRequestDto(email = "", password = "password123")

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `login should return 400 when password is blank`() {
        val request = LoginRequestDto(email = "test@example.com", password = "")

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // -- /register ------------

    @Test
    fun `register should return 201 Created when valid request`() {
        val request = RegisterRequestDto(
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "password123",
            role = UserRole.TRAVELER
        )
        val response = RegisterResponseDto(
            token = null,
            refreshToken = null,
            user = userDto
        )

        every { authService.register(any()) } returns response

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
    }

    @Test
    fun `register should return 400 when email is invalid`() {
        val request = RegisterRequestDto(
            email = "not-an-email",
            firstName = "Test",
            lastName = "User",
            password = "password123",
            role = UserRole.TRAVELER
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `register should return 400 when firstName is blank`() {
        val request = RegisterRequestDto(
            email = "test@example.com",
            firstName = "",
            lastName = "User",
            password = "password123",
            role = UserRole.TRAVELER
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // -- /refresh ------------------

    @Test
    fun `refresh should return 200 and new tokens when valid refresh token`() {
        val request = RefreshTokenRequestDto(refreshToken = "jwt.refresh.token.here")
        val response = RefreshTokenResponseDto(
            accessToken = "new.access.token",
            refreshToken = "new.refresh.token"
        )

        every { authService.refreshAccessToken(any()) } returns response

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("new.access.token"))
            .andExpect(jsonPath("$.refreshToken").value("new.refresh.token"))
    }

    // -- /verify-code ----

    @Test
    fun `verify code should return 200 and tokens when valid code`() {
        val response = LoginResponseDto(
            token = "jwt.token.here",
            refreshToken = "jwt.refresh.token.here",
            user = userDto
        )

        every { authService.verifyEmailByCode("test@example.com", "123456") } returns response

        mockMvc.perform(
            post("/api/auth/verify-code")
                .param("email", "test@example.com")
                .param("code", "123456")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt.token.here"))
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
    }

    // -- /resend-code ----------------------

    @Test
    fun `resend code should return 200 when email exists`() {
        justRun { authService.resendVerificationCode("test@example.com") }

        mockMvc.perform(
            post("/api/auth/resend-code")
                .param("email", "test@example.com")
        )
            .andExpect(status().isOk)
    }

    // -- /google ---------------------

    @Test
    fun `google login should return 200 and tokens when valid id token`() {
        val response = LoginResponseDto(
            token = "jwt.token.here",
            refreshToken = "jwt.refresh.token.here",
            user = userDto
        )

        every { authService.googleLogin("google-id-token", null) } returns response

        mockMvc.perform(
            post("/api/auth/google")
                .param("idToken", "google-id-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt.token.here"))
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
    }

    @Test
    fun `google login should return 200 when role is provided for new user`() {
        val response = LoginResponseDto(
            token = "jwt.token.here",
            refreshToken = "jwt.refresh.token.here",
            user = userDto
        )

        every { authService.googleLogin("google-id-token", UserRole.TRAVELER) } returns response

        mockMvc.perform(
            post("/api/auth/google")
                .param("idToken", "google-id-token")
                .param("role", "TRAVELER")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt.token.here"))
    }
}