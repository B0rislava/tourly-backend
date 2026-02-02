package com.tourly.core.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Column
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(name = "reviews", uniqueConstraints = [UniqueConstraint(columnNames = ["booking_id"])])
data class ReviewEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    val reviewer: UserEntity,

    @ManyToOne(optional = false)
    @JoinColumn(name = "guide_id", nullable = false)
    val guide: UserEntity,

    @ManyToOne(optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    val tour: TourEntity,

    @Column(name = "booking_id", nullable = false, unique = true)
    val bookingId: Long,

    @Column(name = "tour_rating", nullable = false)
    val tourRating: Int,

    @Column(name = "guide_rating", nullable = false)
    val guideRating: Int,

    @Column(name = "comment", length = 2000)
    val comment: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
