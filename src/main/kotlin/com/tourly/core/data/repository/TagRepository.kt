package com.tourly.core.data.repository

import com.tourly.core.data.entity.TagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<TagEntity, Long> {
    fun findByName(name: String): TagEntity?
    fun findAllByNameIn(names: List<String>): List<TagEntity>
}
