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

    // Используем ViewBinding для привязки представлений
    private val binding by viewBinding(FragmentSingInBinding::bind)

    // Инициализируем ViewModel для обработки логики входа
    private val viewModel: SingInViewModel by viewModels()

    // Инъекция зависимости AccountManagerHelper
    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper

    // Переменные для работы с Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инфляция макета для фрагмента входа
        return inflater.inflate(R.layout.fragment_sing_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Наблюдение за состоянием ViewModel
        observeViewModel()

        // Установка обработчиков событий для ввода
        setOnFocusChangeListener()

        // Настройка Google Sign-In
        configureGoogleSignIn()

        // Инициализация Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
    }

    // Конфигурация параметров Google Sign-In
    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Запрос токена ID
            .requestEmail() // Запрос адреса электронной почты
            .build()

        // Создание клиента для Google Sign-In
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    // Наблюдение за состоянием ViewModel для обновления UI
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is SingInState.Loading -> {
                        // Здесь можно показать индикатор загрузки
                    }

                    is SingInState.Success -> {
                        // Уведомление об успешном входе
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        // Переход к следующему фрагменту
                        findNavController().navigate(R.id.action_singInFragment_to_blankFragment)
                    }

                    is SingInState.Error -> {
                        // Уведомление об ошибке входа
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }

                    is SingInState.Idle -> {}
                }
            }
        }
    }

    // Установка обработчиков событий для ввода данных
    private fun setOnFocusChangeListener() {
        // Показать диалог выбора учетной записи при получении фокуса на поле email
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showAccountDialog()
            }
        }

        // Переход к экрану регистрации
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_singInFragment_to_registerFragment)
        }

        // Обработка входа с помощью email и пароля
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.send(SingInIntent.SignInWithEmail(email, password))
            } else {
                // Уведомление о необходимости ввода данных
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Вход с помощью Google Sign-In
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    // Запуск процесса входа с помощью Google
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Обработка результата входа
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    // Обработка результата входа в систему
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // Получение учетной записи Google
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken // Получение токена ID
            firebaseAuthWithGoogle(idToken ?: "", account.email ?: "") // Вход в Firebase
        } catch (e: ApiException) {
            // Уведомление об ошибке при входе
            Toast.makeText(requireContext(), "Sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    // Аутентификация пользователя в Firebase с помощью токена Google
    private fun firebaseAuthWithGoogle(idToken: String, email: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseToken = firebaseAuth.currentUser?.getIdToken(false)?.result?.token

                if (firebaseToken != null) {
                    saveTokenToAccountManager(firebaseToken, email)
                    Toast.makeText(requireContext(), "Successfully signed in with Google", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_singInFragment_to_blankFragment)
                } else {
                    Toast.makeText(requireContext(), "Failed to get Firebase token", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Уведомление об ошибке аутентификации
                Toast.makeText(requireContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Сохранение токена в AccountManager
    private fun saveTokenToAccountManager(token: String, email: String) {
        val account = Account(email, "com.nur.uss.account")
        val encryptedToken = accountManagerHelper.keyStoreManager.encrypt(token)
        val existingAccounts = accountManagerHelper.getAccountsByType("com.nur.uss.account")

        if (existingAccounts.any { it.name == email }) {
            // Если учетная запись уже существует, обновляем токен
            accountManagerHelper.saveToken(account, encryptedToken)
            Toast.makeText(requireContext(), "Token updated in AccountManager", Toast.LENGTH_SHORT).show()
        } else {
            // Добавление новой учетной записи
            accountManagerHelper.addAccount(email, encryptedToken)
            Toast.makeText(requireContext(), "Account added to AccountManager", Toast.LENGTH_SHORT).show()
        }
    }

    // Показ диалогового окна для выбора существующих учетных записей
    private fun showAccountDialog() {
        val accounts = accountManagerHelper.getAccountsByType("com.nur.uss.account")

        if (accounts.isNotEmpty()) {
            val accountNames = accounts.map { it.name }.toTypedArray()
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose Account")

            // Обработчик выбора учетной записи
            builder.setItems(accountNames) { _, which ->
                val selectedAccount = accounts[which]
                binding.etEmail.setText(selectedAccount.name)

                // Попытка получить токен из AccountManager
                val encryptedToken = accountManagerHelper.getToken(selectedAccount)
                if (encryptedToken != null) {
                    try {
                        // Расшифровка токена с помощью KeyStoreManager
                        val decryptedToken = accountManagerHelper.keyStoreManager.decrypt(encryptedToken)
                        binding.etPassword.setText(decryptedToken) // Установка расшифрованного пароля в поле ввода
                    } catch (e: Exception) {
                        // Уведомление об ошибке при расшифровке токена
                        Toast.makeText(requireContext(), "Failed to decrypt token", Toast.LENGTH_SHORT).show()
                        binding.etPassword.setText("") // Очищаем поле пароля
                    }
                } else {
                    binding.etPassword.setText("") // Очищаем поле пароля, если токен не найден
                }
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            builder.show()
        } else {
            // Уведомление о том, что нет доступных учетных записей
            Toast.makeText(requireContext(), "No available accounts", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001 // Код запроса для входа с помощью Google
    }
}
