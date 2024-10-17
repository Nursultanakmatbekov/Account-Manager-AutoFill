package com.nur.uss.ui.fragments.singin

import android.accounts.Account
import android.content.Intent
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nur.uss.R
import com.nur.uss.databinding.FragmentSingInBinding
import com.nur.uss.utils.AccountManagerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SingInFragment : Fragment() {

    private val binding by viewBinding(FragmentSingInBinding::bind)
    private val viewModel: SingInViewModel by viewModels()


    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

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
        configureGoogleSignIn()
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
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

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken
            firebaseAuthWithGoogle(idToken ?: "", account.email ?: "")
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, email: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseToken = firebaseAuth.currentUser?.getIdToken(false)?.result?.token

                if (firebaseToken != null) {
                    saveTokenToAccountManager(firebaseToken, email)
                    Toast.makeText(
                        requireContext(),
                        "Successfully signed in with Google",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_singInFragment_to_blankFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to get Firebase token",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(requireContext(), "Authentication Failed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun saveTokenToAccountManager(token: String, email: String) {
        val account = Account(email, "com.nur.uss.account")
        val encryptedToken = accountManagerHelper.keyStoreManager.encrypt(token)
        val existingAccounts = accountManagerHelper.getAccountsByType("com.nur.uss.account")
        if (existingAccounts.any { it.name == email }) {
            accountManagerHelper.saveToken(account, encryptedToken)
            Toast.makeText(requireContext(), "Token updated in AccountManager", Toast.LENGTH_SHORT)
                .show()
        } else {
            accountManagerHelper.addAccount(email, encryptedToken)
            Toast.makeText(requireContext(), "Account added to AccountManager", Toast.LENGTH_SHORT)
                .show()
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

                val encryptedToken = accountManagerHelper.getToken(selectedAccount)
                if (encryptedToken != null) {
                    try {
                        val decryptedToken =
                            accountManagerHelper.keyStoreManager.decrypt(encryptedToken)
                        binding.etPassword.setText(decryptedToken)
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to decrypt token",
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

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
