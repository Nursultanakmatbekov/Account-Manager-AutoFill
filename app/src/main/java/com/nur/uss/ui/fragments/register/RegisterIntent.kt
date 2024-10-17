package com.nur.uss.ui.fragments.register

// Состояния намерений для регистрации
sealed class RegisterIntent {
    // Намерение регистрации с использованием электронной почты и пароля
    data class RegisterWithEmail(val email: String, val password: String) : RegisterIntent()
}
