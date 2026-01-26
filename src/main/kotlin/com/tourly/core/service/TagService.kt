package com.tourly.core.service

import com.tourly.core.api.dto.tour.TagDto
import com.tourly.core.data.repository.TagRepository
import com.tourly.core.mapper.TagMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val tagRepository: TagRepository
) {

    @Transactional(readOnly = true)
    fun getAllTags(): List<TagDto> {
        return tagRepository.findAll()
            .map { TagMapper.toDto(it) }
    }
}
