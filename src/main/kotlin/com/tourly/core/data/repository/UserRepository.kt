package com.tourly.core.data.repository

import com.tourly.core.data.entity.UserEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {

    fun findByEmail(email: String): UserEntity?

    fun existsByEmail(email: String): Boolean

    @Modifying
    @Query(value = "DELETE FROM saved_tours WHERE tour_id IN :tourIds", nativeQuery = true)
    fun deleteSavedToursByTourIds(tourIds: List<Long>)

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.id = :id")
    fun findByIdForUpdate(id: Long): Optional<UserEntity>
}