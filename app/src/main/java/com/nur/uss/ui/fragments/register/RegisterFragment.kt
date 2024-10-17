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

    // Используем View Binding для доступа к элементам интерфейса
    private val binding by viewBinding(FragmentRegisterBinding::bind)

    // Получаем экземпляр ViewModel для обработки логики регистрации
    private val viewModel: RegisterViewModel by viewModels()

    // Внедряем зависимости для работы с AccountManager и KeyStore
    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper
    @Inject
    lateinit var keyStoreManager: KeyStoreManager

    // Создаем View фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    // Вызывается после создания View
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()  // Подписываемся на изменения состояния ViewModel
        setOnClickListener() // Устанавливаем обработчик нажатия кнопки
    }

    // Наблюдение за состоянием регистрации
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is RegisterState.Loading -> {
                        // Здесь можно добавить индикатор загрузки
                    }
                    is RegisterState.Success -> {
                        // При успешной регистрации
                        val email = binding.etEmail.text.toString()
                        val password = binding.etPassword.text.toString()
                        // Шифруем пароль перед сохранением
                        val encryptedPassword = keyStoreManager.encrypt(password)
                        // Добавляем аккаунт с зашифрованным паролем
                        accountManagerHelper.addAccount(email, encryptedPassword)
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        // Переход к экрану входа
                        findNavController().navigate(R.id.action_registerFragment_to_singInFragment)
                    }
                    is RegisterState.Error -> {
                        // Отображаем ошибку при регистрации
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    is RegisterState.Idle -> {
                        // Состояние ожидания, можно оставить пустым
                    }
                }
            }
        }
    }

    // Установка обработчиков нажатий
    private fun setOnClickListener() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Проверяем, что поля не пустые
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Отправляем намерение на регистрацию с email и паролем
                viewModel.send(RegisterIntent.RegisterWithEmail(email, password))
            } else {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
