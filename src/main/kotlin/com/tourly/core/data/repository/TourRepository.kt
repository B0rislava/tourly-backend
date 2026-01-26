package com.tourly.core.data.repository

import com.tourly.core.data.entity.TourEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TourRepository : JpaRepository<TourEntity, Long>, JpaSpecificationExecutor<TourEntity> {
    @EntityGraph(attributePaths = ["guide", "tags"])
    override fun findAll(spec: Specification<TourEntity>, sort: Sort): List<TourEntity>

    fun findAllByGuideIdOrderByCreatedAtDesc(guideId: Long): List<TourEntity>
}
