package com.tourly.core.data.entity

import com.tourly.core.data.enumeration.UserRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "email", unique = true, nullable = false)
    var email: String,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(nullable = false)
    var password: String,

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole,

    @Column(name = "profile_picture_url")
    var profilePictureUrl: String?
)
