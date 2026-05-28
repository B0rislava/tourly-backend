package com.tourly.core.data.specification

import com.tourly.core.config.Constants
import com.tourly.core.data.entity.TagEntity
import com.tourly.core.data.entity.TourEntity
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate

object TourSpecification {

    fun hasStatus(status: String): Specification<TourEntity> {
        return Specification { root, _, cb ->
            cb.equal(root.get<String>("status"), status)
        }
    }

    fun hasLocation(location: String?): Specification<TourEntity>? {
        return location?.takeIf { it.isNotBlank() }?.let {
            Specification { root, _, cb ->
                cb.like(cb.lower(root.get("location")), "%${it.trim().lowercase()}%")
            }
        }
    }

    fun hasTags(tagNames: List<String>?): Specification<TourEntity>? {
        return tagNames?.takeIf { it.isNotEmpty() }?.let { names ->
            Specification { root, query, _ ->
                // Ensure distinct results when filtering by tags
                query?.distinct(true)

                val tagJoin: Join<TourEntity, TagEntity> = root.join("tags", JoinType.INNER)
                tagJoin.get<String>("name").`in`(names.map { it.trim() })
            }
        }
    }

    fun hasPriceRange(minPrice: Double?, maxPrice: Double?): Specification<TourEntity>? {
        if (minPrice == null && maxPrice == null) return null

        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            minPrice?.let {
                require(it >= 0) { "Minimum price cannot be negative" }
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerPerson"), it))
            }

            maxPrice?.let {
                require(it >= 0) { "Maximum price cannot be negative" }
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerPerson"), it))
            }

            // Validate range if both are provided
            if (minPrice != null && maxPrice != null) {
                require(minPrice <= maxPrice) { "Minimum price cannot exceed maximum price" }
            }

            cb.and(*predicates.toTypedArray())
        }
    }

    fun hasMinRating(minRating: Double?): Specification<TourEntity>? {
        return minRating?.let {
            require(it in 0.0..5.0) { "Rating must be between 0 and 5" }
            Specification { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("rating"), it)
            }
        }
    }

    fun hasScheduledDateRange(after: LocalDate?, before: LocalDate?): Specification<TourEntity>? {
        if (after == null && before == null) return null

        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            after?.let { predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledDate"), it)) }
            before?.let { predicates.add(cb.lessThanOrEqualTo(root.get("scheduledDate"), it)) }

            // Validate range if both are provided
            if (after != null && before != null) {
                require(!after.isAfter(before)) { "Start date cannot be after end date" }
            }

            cb.and(*predicates.toTypedArray())
        }
    }

    fun hasMaxGroupSize(maxSize: Int?): Specification<TourEntity>? {
        return maxSize?.let {
            require(it > 0) { "Maximum group size must be positive" }
            Specification { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("maxGroupSize"), it)
            }
        }
    }

    fun hasAvailableSpots(minAvailable: Int?): Specification<TourEntity>? {
        return minAvailable?.let {
            require(it >= 0) { "Minimum available spots cannot be negative" }
            Specification { root, _, cb ->
                // Assuming you have a field for current bookings or available spots
                cb.greaterThanOrEqualTo(
                    cb.diff(root.get("maxGroupSize"), root.get("currentBookings")),
                    it
                )
            }
        }
    }

    fun hasTextSearch(query: String?): Specification<TourEntity>? {
        return query?.takeIf { it.isNotBlank() }?.let { searchQuery ->
            Specification { root, _, cb ->
                val searchPattern = "%${searchQuery.trim().lowercase()}%"
                cb.or(
                    cb.like(cb.lower(root.get("title")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern),
                    cb.like(cb.lower(root.get("location")), searchPattern)
                )
            }
        }
    }

    fun hasGuideId(guideId: Long?): Specification<TourEntity>? {
        return guideId?.let {
            Specification { root, _, cb ->
                cb.equal(root.get<Long>("guideId"), it)
            }
        }
    }

    /**
     * Combines all filter specifications into a single specification
     */
    fun buildSpecification(
        status: String = Constants.TourStatus.ACTIVE,
        location: String? = null,
        tagNames: List<String>? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        minRating: Double? = null,
        scheduledAfter: LocalDate? = null,
        scheduledBefore: LocalDate? = null,
        maxGroupSize: Int? = null,
        minAvailableSpots: Int? = null,
        textSearch: String? = null,
        guideId: Long? = null
    ): Specification<TourEntity> {
        var spec = Specification.where(hasStatus(status))

        // Apply all optional filters
        listOfNotNull(
            hasLocation(location),
            hasTags(tagNames),
            hasPriceRange(minPrice, maxPrice),
            hasMinRating(minRating),
            hasScheduledDateRange(scheduledAfter, scheduledBefore),
            hasMaxGroupSize(maxGroupSize),
            hasAvailableSpots(minAvailableSpots),
            hasTextSearch(textSearch),
            hasGuideId(guideId)
        ).forEach { spec = spec.and(it) }

        return spec
    }
}