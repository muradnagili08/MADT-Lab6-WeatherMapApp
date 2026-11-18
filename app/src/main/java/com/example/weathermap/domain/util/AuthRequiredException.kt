package com.example.weathermap.domain.util

class AuthRequiredException(
    message: String = "Authentication required: user must be signed in to perform this operation."
) : SecurityException(message)
