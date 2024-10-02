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

@HiltViewModel
class SingInViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<SingInState>(SingInState.Idle)
    val state: StateFlow<SingInState> get() = _state

    private val _intentChannel = Channel<SingInIntent>(Channel.UNLIMITED)
    val intents = _intentChannel.receiveAsFlow()

    init {
        processIntents()
    }

    fun send(intent: SingInIntent) {
        viewModelScope.launch {
            _intentChannel.send(intent)
        }
    }

    private fun processIntents() {
        viewModelScope.launch {
            intents.collect { intent ->
                when (intent) {
                    is SingInIntent.SignInWithEmail -> signInWithEmail(
                        intent.email,
                        intent.password
                    )
                }
            }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        _state.value = SingInState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _state.value = SingInState.Success("Login successful!")
                    } else {
                        _state.value = SingInState.Error(task.exception?.message ?: "Login failed.")
                    }
                }
        }
    }
}
