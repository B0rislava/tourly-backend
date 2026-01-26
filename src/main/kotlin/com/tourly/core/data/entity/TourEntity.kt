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
    val title: String,

    @Column(name = "description", nullable = false, length = 2000)
    val description: String,

    @Column(name = "location", nullable = false)
    val location: String,

    @Column(name = "duration", nullable = false)
    val duration: String,

    @Column(name = "max_group_size", nullable = false)
    val maxGroupSize: Int,

    @Column(name = "available_spots", nullable = false, columnDefinition = "integer default 0")
    var availableSpots: Int = maxGroupSize,

    @Column(name = "price_per_person", nullable = false)
    val pricePerPerson: Double,

    @Column(name = "whats_included", nullable = true, length = 1000)
    val whatsIncluded: String?,

    @Column(name = "scheduled_date", nullable = true)
    val scheduledDate: LocalDate? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "status", nullable = false)
    val status: String = "ACTIVE",

    @Column(name = "rating")
    val rating: Double = 0.0,

    @Column(name = "reviews_count")
    val reviewsCount: Int = 0,

    @Column(name = "meeting_point")
    val meetingPoint: String? = null,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "cancellation_policy", length = 1000)
    val cancellationPolicy: String? = null,

    @Column(name = "latitude")
    val latitude: Double? = null,

    @Column(name = "longitude")
    val longitude: Double? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tour_tags",
        joinColumns = [JoinColumn(name = "tour_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<TagEntity> = mutableSetOf()
)
