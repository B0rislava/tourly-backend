package com.tourly.core.service

import com.tourly.core.api.dto.auth.RegisterRequestDto
import com.tourly.core.api.dto.auth.LoginRequestDto
import com.tourly.core.data.entity.UserEntity
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.security.JWTUtil
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockKExtension::class)
class AuthServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @MockK
    private lateinit var authenticationManager: AuthenticationManager

    @MockK
    private lateinit var jwtUtil: JWTUtil

    @InjectMockKs
    private lateinit var authService: AuthService

    private lateinit var registerRequest: RegisterRequestDto

    @BeforeEach
    fun setup() {
        registerRequest = RegisterRequestDto(
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "password123",
            role = UserRole.TRAVELER
        )
    }

    @Test
    fun `register should throw exception when email already exists`() {

        every { userRepository.existsByEmail("test@example.com") } returns true

        val exception = assertThrows(APIException::class.java) {
            authService.register(registerRequest)
        }
        assertEquals(ErrorCode.BAD_REQUEST, exception.errorCode)
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `register should save user successfully when valid data provided`() {

        every { userRepository.existsByEmail("test@example.com") } returns false
        every { passwordEncoder.encode("password123") } returns "encoded_password"
        every { userRepository.save(any()) } returns UserEntity(
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "encoded_password",
            role = UserRole.TRAVELER,
            profilePictureUrl = null
        )

        val response = authService.register(registerRequest)

        assertEquals("User registered successfully", response.message)
        assertEquals("test@example.com", response.email)
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `login should return token and user dto when credentials are valid`() {
        val loginRequest = LoginRequestDto(email = "test@example.com", password = "password123")
        val userEntity = UserEntity(
            id = 1L,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "encoded_password",
            role = UserRole.TRAVELER,
            profilePictureUrl = null
        )

        every { authenticationManager.authenticate(any()) } returns
                UsernamePasswordAuthenticationToken("test@example.com", "password123")
        every { userRepository.findByEmail("test@example.com") } returns userEntity
        every { jwtUtil.generateToken("test@example.com", listOf("TRAVELER")) } returns "jwt.token.here"

        val response = authService.login(loginRequest)

        assertEquals("jwt.token.here", response.token)
        assertEquals("test@example.com", response.user.email)
        assertNotNull(response.user)
    }

    @Test
    fun `login should throw UNAUTHORIZED when credentials are invalid`() {
        val loginRequest = LoginRequestDto(email = "test@example.com", password = "wrongpassword")

        every { authenticationManager.authenticate(any()) } throws RuntimeException("Bad credentials")

        val exception = assertThrows(APIException::class.java) {
            authService.login(loginRequest)
        }
        assertEquals(ErrorCode.UNAUTHORIZED, exception.errorCode)
    }
}
