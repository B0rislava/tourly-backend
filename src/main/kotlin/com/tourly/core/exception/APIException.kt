package com.tourly.core.exception

class APIException(
    val errorCode: ErrorCode,
    val description: String? = null
) : RuntimeException(description ?: errorCode.message)
