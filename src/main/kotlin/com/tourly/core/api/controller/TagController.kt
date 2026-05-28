package com.tourly.core.api.controller

import com.tourly.core.api.dto.tour.TagDto
import com.tourly.core.service.TagService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Tags", description = "Endpoints for retrieving tour tags")
@RestController
@RequestMapping("/api/tags")
class TagController(
    private val tagService: TagService
) {

    @Operation(summary = "Get all tags", description = "Fetches a list of all available tour tags")
    @GetMapping
    fun getAllTags(): ResponseEntity<List<TagDto>> {
        val tags = tagService.getAllTags()
        return ResponseEntity.ok(tags)
    }
}
