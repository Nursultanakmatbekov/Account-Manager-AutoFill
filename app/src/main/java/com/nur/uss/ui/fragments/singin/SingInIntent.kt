package com.nur.uss.ui.fragments.singin

sealed class SingInIntent {
    data class SignInWithEmail(val email: String, val password: String) : SingInIntent()
}