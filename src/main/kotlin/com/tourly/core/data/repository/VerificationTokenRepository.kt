package com.tourly.core.data.repository

import com.tourly.core.data.entity.VerificationTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VerificationTokenRepository : JpaRepository<VerificationTokenEntity, Long> {
    fun findByToken(token: String): VerificationTokenEntity?
}
