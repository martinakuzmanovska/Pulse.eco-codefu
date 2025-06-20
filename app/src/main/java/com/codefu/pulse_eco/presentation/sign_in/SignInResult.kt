package com.codefu.pulse_eco.presentation.sign_in

class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val points: Int = 0,
    val itemIds: List<String> = emptyList()
)

