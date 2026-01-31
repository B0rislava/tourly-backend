package com.tourly.core.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "tours")
data class TourEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @ManyToOne(optional = false)
    @JoinColumn(name = "guide_id", nullable = false)
    val guide: UserEntity,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "description", nullable = false, length = 2000)
    var description: String,

    @Column(name = "location", nullable = false)
    var location: String,

    @Column(name = "duration", nullable = false)
    var duration: String,

    @Column(name = "max_group_size", nullable = false)
    var maxGroupSize: Int,

    @Column(name = "available_spots", nullable = false, columnDefinition = "integer default 0")
    var availableSpots: Int = maxGroupSize,

    @Column(name = "price_per_person", nullable = false)
    var pricePerPerson: Double,

    @Column(name = "whats_included", nullable = true, length = 1000)
    var whatsIncluded: String?,

    @Column(name = "scheduled_date", nullable = true)
    var scheduledDate: LocalDate? = null,

    @Column(name = "start_time", nullable = true)
    var startTime: LocalTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "status", nullable = false)
    var status: String = "ACTIVE",

    @Column(name = "rating")
    val rating: Double? = 0.0,

    @Column(name = "reviews_count")
    val reviewsCount: Int? = 0,

    @Column(name = "meeting_point")
    var meetingPoint: String? = null,

    @Column(name = "image_url")
    var imageUrl: String? = null,


    @Column(name = "latitude")
    var latitude: Double? = null,

    @Column(name = "longitude")
    var longitude: Double? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tour_tags",
        joinColumns = [JoinColumn(name = "tour_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<TagEntity> = mutableSetOf()
)
