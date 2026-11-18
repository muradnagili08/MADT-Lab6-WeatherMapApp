package com.example.weathermap.domain.repository

import com.example.weathermap.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: User?
    val isUserAuthenticated: Boolean
    val authState: Flow<User?>

    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
    fun signOut()
}
