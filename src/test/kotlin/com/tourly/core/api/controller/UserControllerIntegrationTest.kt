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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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

    // -- GET /{id} -----

    @Test
    fun `getUserProfile should return 200 with user dto when authenticated`() {
        val mockUserDto = UserDto(
            id = 2L,
            email = "other@example.com",
            firstName = "Other",
            lastName = "User",
            role = UserRole.TRAVELER,
            profilePictureUrl = null
        )

        val customUserDetails = CustomUserDetails(
            userId = 1L,
            email = "traveler@example.com",
            password = "pwd",
            role = UserRole.TRAVELER
        )

        every { userService.getUserProfileById(2L, 1L) } returns mockUserDto

        mockMvc.perform(
            get("/api/users/2")
                .with(user(customUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("other@example.com"))
    }

    @Test
    fun `getUserProfile should return 200 when unauthenticated`() {
        val mockUserDto = UserDto(
            id = 2L,
            email = "other@example.com",
            firstName = "Other",
            lastName = "User",
            role = UserRole.TRAVELER,
            profilePictureUrl = null
        )

        every { userService.getUserProfileById(2L, null) } returns mockUserDto

        mockMvc.perform(
            get("/api/users/2")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("other@example.com"))
    }

    // -- PUT /me (validation) -----------------

    @Test
    fun `updateProfile should return 400 when email is invalid`() {
        val request = UpdateProfileRequestDto(
            email = "not-an-email",
            firstName = "Test",
            lastName = "User"
        )

        val customUserDetails = CustomUserDetails(
            userId = 1L,
            email = "traveler@example.com",
            password = "pwd",
            role = UserRole.TRAVELER
        )

        mockMvc.perform(
            put("/api/users/me")
                .with(user(customUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `updateProfile should return 400 when firstName is blank`() {
        val request = UpdateProfileRequestDto(
            email = "traveler@example.com",
            firstName = "",
            lastName = "User"
        )

        val customUserDetails = CustomUserDetails(
            userId = 1L,
            email = "traveler@example.com",
            password = "pwd",
            role = UserRole.TRAVELER
        )

        mockMvc.perform(
            put("/api/users/me")
                .with(user(customUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }


    // -- POST /{id}/follow --------

    @Test
    fun `followUser should return 200 when valid`() {
        val customUserDetails = CustomUserDetails(
            userId = 1L,
            email = "traveler@example.com",
            password = "pwd",
            role = UserRole.TRAVELER
        )

        every { userService.followUser(1L, 2L) } returns Unit

        mockMvc.perform(
            post("/api/users/2/follow")
                .with(user(customUserDetails))
        )
            .andExpect(status().isOk)
    }

    // -- DELETE /{id}/follow -------------------

    @Test
    fun `unfollowUser should return 200 when valid`() {
        val customUserDetails = CustomUserDetails(
            userId = 1L,
            email = "traveler@example.com",
            password = "pwd",
            role = UserRole.TRAVELER
        )

        every { userService.unfollowUser(1L, 2L) } returns Unit

        mockMvc.perform(
            delete("/api/users/2/follow")
                .with(user(customUserDetails))
        )
            .andExpect(status().isOk)
    }

    // -- DELETE /me --------------

    @Test
    fun `deleteProfile should return 204 when valid`() {
        val customUserDetails = CustomUserDetails(
            userId = 1L,
            email = "traveler@example.com",
            password = "pwd",
            role = UserRole.TRAVELER
        )

        every { userService.deleteUser(1L) } returns Unit

        mockMvc.perform(
            delete("/api/users/me")
                .with(user(customUserDetails))
        )
            .andExpect(status().isNoContent)
    }
}