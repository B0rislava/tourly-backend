package com.tourly.core.data.repository

import com.tourly.core.data.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {

    fun findByToken(token: String): RefreshTokenEntity?
    fun deleteAllByUserId(userId: Long)
}
