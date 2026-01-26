package com.tourly.core.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "bookings")
data class BookingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    val tour: TourEntity,

    @Column(name = "number_of_participants", nullable = false)
    val numberOfParticipants: Int = 1,

    @Column(name = "booking_date", nullable = false)
    val bookingDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "status", nullable = false)
    var status: String = "CONFIRMED"
)
