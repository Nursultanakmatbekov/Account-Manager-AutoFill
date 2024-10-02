package com.nur.uss.ui.fragments.singin

sealed class SingInState {
    object Idle : SingInState()
    object Loading : SingInState()
    data class Success(val message: String) : SingInState()
    data class Error(val error: String) : SingInState()
}
