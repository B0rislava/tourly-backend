package com.tourly.core.config

import com.tourly.core.data.entity.TagEntity
import com.tourly.core.data.repository.TagRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    @Bean
    fun initTags(tagRepository: TagRepository): CommandLineRunner {
        return CommandLineRunner {
            if (tagRepository.count() == 0L) {
                logger.info("Initializing default tags...")
                
                val defaultTags = listOf(
                    TagEntity(name = "ADVENTURE", displayName = "Adventure"),
                    TagEntity(name = "CULTURE", displayName = "Culture"),
                    TagEntity(name = "NATURE", displayName = "Nature"),
                    TagEntity(name = "FOOD", displayName = "Food"),
                    TagEntity(name = "RELAX", displayName = "Relax"),
                    TagEntity(name = "HISTORY", displayName = "History"),
                    TagEntity(name = "SPORTS", displayName = "Sports"),
                    TagEntity(name = "WILDLIFE", displayName = "Wildlife"),
                    TagEntity(name = "PHOTOGRAPHY", displayName = "Photography"),
                    TagEntity(name = "NIGHTLIFE", displayName = "Nightlife")
                )
                
                tagRepository.saveAll(defaultTags)
                logger.info("Successfully initialized ${defaultTags.size} tags")
            } else {
                logger.info("Tags already initialized (${tagRepository.count()} tags found)")
            }
        }
    }
}
