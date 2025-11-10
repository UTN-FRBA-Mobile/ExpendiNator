package ar.edu.utn.frba.expendinator.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val user: UserInfo)

@Serializable
data class UserInfo(val id: String, val email: String)
