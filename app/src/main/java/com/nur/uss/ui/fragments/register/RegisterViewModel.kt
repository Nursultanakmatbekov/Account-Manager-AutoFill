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

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> get() = _state

    private val _intentChannel = Channel<RegisterIntent>(Channel.UNLIMITED)
    val intents = _intentChannel.receiveAsFlow()

    init {
        processIntents()
    }

    fun send(intent: RegisterIntent) {
        viewModelScope.launch {
            _intentChannel.send(intent)
        }
    }

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

    private fun registerUser(email: String, password: String) {
        _state.value = RegisterState.Loading

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