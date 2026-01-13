package com.tourly.core.data.repository

import com.tourly.core.data.entity.TourEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TourRepository : JpaRepository<TourEntity, Long> {
    fun findAllByGuideIdOrderByCreatedAtDesc(guideId: Long): List<TourEntity>
    fun findAllByStatusOrderByCreatedAtDesc(status: String): List<TourEntity>
}
