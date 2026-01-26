package com.tourly.core.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table


@Entity
@Table(name = "tags")
data class TagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @Column(name = "name", unique = true, nullable = false)
    val name: String,

    @Column(name = "display_name", nullable = false)
    val displayName: String,

    @Column(name = "is_system", nullable = false)
    val isSystem: Boolean = true
)
