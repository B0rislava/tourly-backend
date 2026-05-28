package com.tourly.core.service

import com.tourly.core.api.dto.auth.RegisterRequestDto
import com.tourly.core.api.dto.auth.LoginRequestDto
import com.tourly.core.data.entity.UserEntity
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.data.repository.RefreshTokenRepository
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.data.repository.VerificationTokenRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.security.JWTUtil
import io.mockk.every
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
import org.springframework.security.authentication.BadCredentialsException

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

    @MockK
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @MockK
    private lateinit var emailService: EmailService

    @MockK
    private lateinit var verificationTokenRepository: VerificationTokenRepository

    private lateinit var authService: AuthService

    private lateinit var registerRequest: RegisterRequestDto

    @BeforeEach
    fun setup() {
        authService = AuthService(
            userRepository = userRepository,
            refreshTokenRepository = refreshTokenRepository,
            verificationTokenRepository = verificationTokenRepository,
            passwordEncoder = passwordEncoder,
            authenticationManager = authenticationManager,
            jwtUtil = jwtUtil,
            emailService = emailService,
            googleClientId = "test-google-client-id"
        )

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
            id = 1L,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "encoded_password",
            role = UserRole.TRAVELER,
            profilePictureUrl = null
        )
        every { verificationTokenRepository.save(any()) } returns com.tourly.core.data.entity.VerificationTokenEntity(
            token = "123456",
            userId = 1L,
            expiresAt = java.time.LocalDateTime.now()
        )
        every { emailService.sendVerificationCode(any(), any()) } returns Unit

        val response = authService.register(registerRequest)

        assertEquals("test@example.com", response.user.email)
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
            profilePictureUrl = null,
            isVerified = true
        )

        every { authenticationManager.authenticate(any()) } returns
                UsernamePasswordAuthenticationToken("test@example.com", "password123")
        every { userRepository.findByEmail("test@example.com") } returns userEntity
        every { jwtUtil.generateAccessToken("test@example.com", listOf("TRAVELER")) } returns "jwt.token.here"
        every { jwtUtil.generateRefreshToken("test@example.com") } returns "jwt.refresh.token.here"
        every { jwtUtil.refreshTokenExpirationMs } returns 604800000L
        every { refreshTokenRepository.save(any()) } returns com.tourly.core.data.entity.RefreshTokenEntity(
            userId = 1L,
            token = "jwt.refresh.token.here",
            expiresAt = java.time.LocalDateTime.now()
        )

        val response = authService.login(loginRequest)

        assertEquals("jwt.token.here", response.token)
        assertEquals("jwt.refresh.token.here", response.refreshToken)
        assertEquals("test@example.com", response.user.email)
        assertNotNull(response.user)
    }

    @Test
    fun `login should throw UNAUTHORIZED when credentials are invalid`() {
        val loginRequest = LoginRequestDto(email = "test@example.com", password = "wrongpassword")
        val userEntity = UserEntity(
            id = 1L,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "encoded_password",
            role = UserRole.TRAVELER,
            profilePictureUrl = null,
            isVerified = true
        )

        every { userRepository.findByEmail("test@example.com") } returns userEntity
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Bad credentials")
        val exception = assertThrows(APIException::class.java) {
            authService.login(loginRequest)
        }
        assertEquals(ErrorCode.UNAUTHORIZED, exception.errorCode)
    }

    @Test
    fun `login should throw EMAIL_NOT_VERIFIED when user is not verified`() {
        val loginRequest = LoginRequestDto(email = "test@example.com", password = "password123")
        val unverifiedUser = UserEntity(
            id = 1L,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            password = "encoded_password",
            role = UserRole.TRAVELER,
            profilePictureUrl = null,
            isVerified = false
        )

        every { userRepository.findByEmail("test@example.com") } returns unverifiedUser

        val exception = assertThrows(APIException::class.java) {
            authService.login(loginRequest)
        }
        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, exception.errorCode)
    }
}
