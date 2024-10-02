package com.nur.uss.ui.fragments.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.nur.uss.R
import com.nur.uss.databinding.FragmentRegisterBinding
import com.nur.uss.utils.AccountManagerHelper
import com.nur.uss.utils.KeyStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val binding by viewBinding(FragmentRegisterBinding::bind)
    private val viewModel: RegisterViewModel by viewModels()

    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper
    @Inject
    lateinit var keyStoreManager: KeyStoreManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setOnClickListener()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is RegisterState.Loading -> {}
                    is RegisterState.Success -> {
                        val email = binding.etEmail.text.toString()
                        val password = binding.etPassword.text.toString()
                        val encryptedPassword = keyStoreManager.encrypt(password)
                        accountManagerHelper.addAccount(email, encryptedPassword)
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_singInFragment)

                    }
                    is RegisterState.Error -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    is RegisterState.Idle -> {}
                }
            }
        }
    }

    private fun setOnClickListener() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.send(RegisterIntent.RegisterWithEmail(email, password))
            } else {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
