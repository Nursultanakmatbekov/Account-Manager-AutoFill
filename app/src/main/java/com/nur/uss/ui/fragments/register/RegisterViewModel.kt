package com.nur.uss.ui.fragments.register

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

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    // Состояние для отслеживания статуса регистрации
    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> get() = _state

    // Канал для получения намерений регистрации
    private val _intentChannel = Channel<RegisterIntent>(Channel.UNLIMITED)
    val intents = _intentChannel.receiveAsFlow()

    init {
        processIntents() // Инициализация обработки намерений
    }

    // Отправка намерения регистрации
    fun send(intent: RegisterIntent) {
        viewModelScope.launch {
            _intentChannel.send(intent)
        }
    }

    // Обработка намерений регистрации
    private fun processIntents() {
        viewModelScope.launch {
            intents.collect { intent ->
                when (intent) {
                    is RegisterIntent.RegisterWithEmail -> registerUser(
                        intent.email,
                        intent.password
                    )
                }
            }
        }
    }

    // Регистрация пользователя
    private fun registerUser(email: String, password: String) {
        _state.value = RegisterState.Loading // Установка состояния загрузки

        viewModelScope.launch(Dispatchers.IO) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _state.value = RegisterState.Success("Registration successful!")
                    } else {
                        _state.value =
                            RegisterState.Error(task.exception?.message ?: "Registration failed")
                    }
                }
        }
    }
}
