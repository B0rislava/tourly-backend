package com.tourly.core.api.controller

import com.ninjasquad.springmockk.MockkBean
import com.tourly.core.api.dto.UpdateProfileRequestDto
import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.security.CustomUserDetails
import com.tourly.core.security.JWTUtil
import com.tourly.core.service.AuthService
import com.tourly.core.service.UserService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tourly.core.security.JWTAuthFilter
import com.tourly.core.service.CustomUserDetailsService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

import org.junit.jupiter.api.BeforeEach
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse

@WebMvcTest(UserController::class)
@Import(UserControllerIntegrationTest.TestSecurityConfig::class)
class UserControllerIntegrationTest {

    @TestConfiguration
    @EnableWebSecurity
    class TestSecurityConfig {
        @Bean
        fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http
                .csrf { it.disable() }
                .authorizeHttpRequests { it.anyRequest().permitAll() }
            return http.build()
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var jwtUtil: JWTUtil

    @MockkBean
    private lateinit var authService: AuthService

    @MockkBean
    @Suppress("unused")
    private lateinit var customUserDetailsService: CustomUserDetailsService


    @MockkBean(relaxed = true)
    @Suppress("unused")
    private lateinit var jwtAuthFilter: JWTAuthFilter

    @BeforeEach
    fun setUp() {
        every { jwtAuthFilter.doFilter(any(), any(), any()) } answers {
            val req = arg<ServletRequest>(0)
            val res = arg<ServletResponse>(1)
            val chain = arg<FilterChain>(2)
            chain.doFilter(req, res)
        }
    }

    @Test
    fun `me should return current user profile`() {
        val mockUserDto = UserDto(
            id = 1L,
            email = "traveler@example.com",
            firstName = "Traveler",
            lastName = "Test",
            role = UserRole.TRAVELER,
            profilePictureUrl = null
        )

        every { userService.getCurrentUserProfile(1L) } returns mockUserDto

        val customUserDetails = CustomUserDetails(
            userId = 1L,
            email = "traveler@example.com",
            password = "pwd",
            role = UserRole.TRAVELER
        )

        mockMvc.perform(
            get("/api/users/me")
                .with(user(customUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("traveler@example.com"))
            .andExpect(jsonPath("$.firstName").value("Traveler"))
    }

    @Test
    fun `updateProfile should return updated user and new token`() {
        val request = UpdateProfileRequestDto(
            email = "traveler@example.com",
            firstName = "NewName",
            lastName = "Test"
        )

        val updatedUserDto = UserDto(
            id = 1L,
            email = "traveler@example.com",
            firstName = "NewName",
            lastName = "Test",
            role = UserRole.TRAVELER,
            profilePictureUrl = null
        )

        val customUserDetails = CustomUserDetails(
            userId = 1L,
            email = "traveler@example.com",
            password = "pwd",
            role = UserRole.TRAVELER
        )

        every { userService.updateProfile(1L, any()) } returns updatedUserDto
        every { jwtUtil.generateAccessToken("traveler@example.com", listOf("TRAVELER")) } returns "new.jwt.token"
        every { authService.createAndSaveRefreshToken(1L, "traveler@example.com") } returns "new.jwt.refresh.token"

        mockMvc.perform(
            put("/api/users/me")
                .with(user(customUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("new.jwt.token"))
            .andExpect(jsonPath("$.user.firstName").value("NewName"))
    }
}