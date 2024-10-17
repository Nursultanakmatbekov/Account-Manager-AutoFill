package com.nur.uss.ui.fragments.singin

// sealed class для определения различных состояний входа
sealed class SingInState {
    object Idle : SingInState() // Состояние бездействия
    object Loading : SingInState() // Состояние загрузки (например, когда идет процесс входа)
    data class Success(val message: String) : SingInState() // Состояние успеха с сообщением
    data class Error(val error: String) : SingInState() // Состояние ошибки с сообщением об ошибке
}
