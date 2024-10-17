package com.nur.uss.ui.fragments.singin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Аннотация Hilt для внедрения зависимостей в ViewModel
@HiltViewModel
class SingInViewModel @Inject constructor(
    private val auth: FirebaseAuth // Внедрение FirebaseAuth для аутентификации
) : ViewModel() {

    // Определение состояния ViewModel с начальным состоянием Idle
    private val _state = MutableStateFlow<SingInState>(SingInState.Idle)
    val state: StateFlow<SingInState> get() = _state // Экспонирование состояния для наблюдения

    // Канал для обработки намерений (интенций)
    private val _intentChannel = Channel<SingInIntent>(Channel.UNLIMITED)
    val intents = _intentChannel.receiveAsFlow() // Поток для получения намерений

    init {
        processIntents() // Запуск обработки намерений в инициализаторе
    }

    // Метод для отправки намерений во ViewModel
    fun send(intent: SingInIntent) {
        viewModelScope.launch {
            _intentChannel.send(intent) // Отправка намерения в канал
        }
    }

    // Обработка входящих намерений
    private fun processIntents() {
        viewModelScope.launch {
            intents.collect { intent ->
                when (intent) {
                    is SingInIntent.SignInWithEmail -> signInWithEmail(
                        intent.email,
                        intent.password
                    ) // Вызов метода входа с email и паролем
                }
            }
        }
    }

    // Метод для входа с помощью email и пароля
    private fun signInWithEmail(email: String, password: String) {
        _state.value = SingInState.Loading // Установка состояния загрузки

        viewModelScope.launch(Dispatchers.IO) { // Запуск в фоновом потоке
            auth.signInWithEmailAndPassword(email, password) // Вход с использованием Firebase
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _state.value = SingInState.Success("Login successful!") // Успешный вход
                    } else {
                        // Ошибка входа, установка соответствующего состояния
                        _state.value = SingInState.Error(task.exception?.message ?: "Login failed.")
                    }
                }
        }
    }
}
