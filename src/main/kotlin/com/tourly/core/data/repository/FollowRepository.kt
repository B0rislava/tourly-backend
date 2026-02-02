package com.tourly.core.data.repository

import com.tourly.core.data.entity.FollowEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FollowRepository : JpaRepository<FollowEntity, Long> {
    
    fun existsByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Boolean
    
    fun deleteByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Int
    
    @Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.followingId = :userId")
    fun countFollowersByUserId(userId: Long): Long
    
    @Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.followerId = :userId")
    fun countFollowingByUserId(userId: Long): Long
    
    @Modifying
    @Query("DELETE FROM FollowEntity f WHERE f.followerId = :userId OR f.followingId = :userId")
    fun deleteAllByUserId(userId: Long)

    @Query("SELECT f.followerId FROM FollowEntity f WHERE f.followingId = :userId")
    fun findFollowerIdsByUserId(userId: Long): List<Long>
}
