package com.tourly.core.data.repository

import com.tourly.core.data.entity.VerificationTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VerificationTokenRepository : JpaRepository<VerificationTokenEntity, Long> {
    fun findByToken(token: String): VerificationTokenEntity?
    
    @Modifying
    @Query("DELETE FROM VerificationTokenEntity v WHERE v.userId = :userId")
    fun deleteByUserId(userId: Long)

    fun findTopByUserIdOrderByExpiresAtDesc(userId: Long): VerificationTokenEntity?
}
