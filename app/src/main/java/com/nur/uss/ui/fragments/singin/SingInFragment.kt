package com.nur.uss.ui.fragments.singin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.nur.uss.R
import com.nur.uss.databinding.FragmentSingInBinding
import com.nur.uss.utils.AccountManagerHelper
import com.nur.uss.utils.KeyStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SingInFragment: Fragment() {

    private val binding by viewBinding(FragmentSingInBinding::bind)
    private val viewModel: SingInViewModel by viewModels()

    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper
    @Inject
    lateinit var keyStoreManager: KeyStoreManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sing_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setOnFocusChangeListener()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is SingInState.Loading -> {}

                    is SingInState.Success -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_singInFragment_to_blankFragment)
                    }

                    is SingInState.Error -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }

                    is SingInState.Idle -> {}
                }
            }
        }
    }

    private fun setOnFocusChangeListener() {
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showAccountDialog()
            }
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_singInFragment_to_registerFragment)
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.send(SingInIntent.SignInWithEmail(email, password))
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showAccountDialog() {
        val accounts = accountManagerHelper.getAccountsByType("com.nur.uss.account")

        if (accounts.isNotEmpty()) {
            val accountNames = accounts.map { it.name }.toTypedArray()
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose Account")

            builder.setItems(accountNames) { _, which ->
                val selectedAccount = accounts[which]
                binding.etEmail.setText(selectedAccount.name)

                val encryptedPassword = accountManagerHelper.getPassword(selectedAccount)
                if (encryptedPassword != null) {
                    try {
                        val decryptedPassword = keyStoreManager.decrypt(encryptedPassword)
                        binding.etPassword.setText(decryptedPassword)
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to decrypt password",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.etPassword.setText("")
                    }
                } else {
                    binding.etPassword.setText("")
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            builder.show()
        } else {
            Toast.makeText(requireContext(), "No available accounts", Toast.LENGTH_SHORT).show()
        }
    }
}