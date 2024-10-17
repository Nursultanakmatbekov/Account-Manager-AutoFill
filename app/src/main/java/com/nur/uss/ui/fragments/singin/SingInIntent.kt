package com.nur.uss.ui.fragments.singin

// sealed class для определения различных интенций входа
sealed class SingInIntent {
    // Интенция для входа с помощью email и пароля
    data class SignInWithEmail(val email: String, val password: String) : SingInIntent()
}
