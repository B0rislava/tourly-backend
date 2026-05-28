package com.tourly.core.service

import com.tourly.core.data.repository.UserRepository
import com.tourly.core.security.CustomUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found with email: $username")
            
        return CustomUserDetails(
            userId = user.id ?: throw IllegalStateException("User ID is null"),
            email = user.email,
            password = user.password,
            role = user.role
        )
    }
}