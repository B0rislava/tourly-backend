package com.tourly.core.api.controller

import com.tourly.core.api.dto.tour.TagDto
import com.tourly.core.service.TagService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tags")
class TagController(
    private val tagService: TagService
) {

    @GetMapping
    fun getAllTags(): ResponseEntity<List<TagDto>> {
        val tags = tagService.getAllTags()
        return ResponseEntity.ok(tags)
    }
}
