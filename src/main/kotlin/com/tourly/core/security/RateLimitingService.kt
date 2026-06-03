package com.tourly.core.security

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimitingService {
    private val cache = ConcurrentHashMap<String, Bucket>()

    fun resolveBucket(ip: String): Bucket {
        return cache.computeIfAbsent(ip) { newBucket() }
    }

    private fun newBucket(): Bucket {
        // Allow 10 requests per minute
        val limit = Bandwidth.builder()
            .capacity(10)
            .refillIntervally(10, Duration.ofMinutes(1))
            .build()
            
        return Bucket.builder()
            .addLimit(limit)
            .build()
    }
}
