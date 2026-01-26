package com.tourly.core.mapper

import com.tourly.core.api.dto.tour.TagDto
import com.tourly.core.data.entity.TagEntity

object TagMapper {
    fun toDto(entity: TagEntity): TagDto {
        return TagDto(
            id = entity.id,
            name = entity.name,
            displayName = entity.displayName
        )
    }
}
