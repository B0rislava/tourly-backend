package com.tourly.core.config

object Constants {
    object Auth {
        const val VERIFICATION_CODE_MIN = 100000
        const val VERIFICATION_CODE_MAX = 999999
        const val VERIFICATION_TOKEN_EXPIRATION_MINUTES = 15L
        const val RESEND_CODE_RATE_LIMIT_SECONDS = 60L
    }

    object Cloudinary {
        const val FOLDER_TOUR_IMAGES = "tour_images"
        const val FOLDER_AVATARS = "avatars"
        const val PREFIX_TOUR = "tour_"
        const val PREFIX_USER = "user_"
    }

    object TourStatus {
        const val ACTIVE = "ACTIVE"
        const val DELETED = "DELETED"
    }

    object BookingStatus {
        const val CONFIRMED = "CONFIRMED"
        const val CANCELLED = "CANCELLED"
        const val COMPLETED = "COMPLETED"
    }
    
    object NotificationType {
        const val TOUR_CANCELLED = "TOUR_CANCELLED"
    }
}
