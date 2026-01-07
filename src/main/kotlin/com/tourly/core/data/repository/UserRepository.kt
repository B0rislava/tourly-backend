package com.tourly.core.data.repository

import com.tourly.core.data.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {

    fun findByEmail(email: String): UserEntity?

    fun existsByEmail(email: String): Boolean
}