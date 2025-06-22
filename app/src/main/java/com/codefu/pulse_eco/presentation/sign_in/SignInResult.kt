package com.codefu.pulse_eco.presentation.sign_in

class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val name: String?,
    val email: String?,
    val points: Int = 0
)