package com.nur.uss.ui.fragments.register

sealed class RegisterIntent {
    data class RegisterWithEmail(val email: String, val password: String) : RegisterIntent()
}