package com.nur.uss.ui.fragments.register

// Sealed class, представляющая различные состояния процесса регистрации
sealed class RegisterState {
    // Состояние, когда ничего не происходит (ожидание)
    object Idle : RegisterState()

    // Состояние, когда идет процесс загрузки
    object Loading : RegisterState()

    // Состояние успешной регистрации с сообщением об успехе
    data class Success(val message: String) : RegisterState()

    // Состояние ошибки с сообщением об ошибке
    data class Error(val error: String) : RegisterState()
}
