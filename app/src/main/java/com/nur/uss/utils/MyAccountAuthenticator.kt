package com.nur.uss.utils

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.util.Log

class MyAccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    // Метод для редактирования свойств учетной записи (не используется)
    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        return null // Возвращаем null, так как редактирование свойств не поддерживается
    }

    // Метод для добавления новой учетной записи
    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        // Создаем новую учетную запись с заданным типом
        val account = Account("new_account", accountType ?: "com.nur.uss.account")
        val accountManager = AccountManager.get(context) // Получаем экземпляр AccountManager

        // Пытаемся добавить учетную запись в AccountManager
        val success = accountManager.addAccountExplicitly(account, "password", null)
        Log.d("MyAccountAuthenticator", "Attempting to add account: $accountType")

        // Проверяем, успешно ли добавлена учетная запись
        return if (success) {
            Log.d("MyAccountAuthenticator", "Account added: ${account.name}")
            // Возвращаем информацию об учетной записи в Bundle
            Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            }
        } else {
            Log.e("MyAccountAuthenticator", "Failed to add account: ${account.name}")
            // Возвращаем сообщение об ошибке в Bundle
            Bundle().apply {
                putString(AccountManager.KEY_ERROR_CODE, "1")
                putString(AccountManager.KEY_ERROR_MESSAGE, "Failed to add account.")
            }
        }
    }

    // Метод для подтверждения учетных данных (не реализован)
    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        return null // Не реализовано
    }

    // Метод для получения токена (не реализован)
    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        return null // Не реализовано
    }

    // Метод для получения метки токена
    override fun getAuthTokenLabel(authTokenType: String): String {
        return authTokenType // Возвращаем тип токена
    }

    // Метод для обновления учетных данных (не реализован)
    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        return null // Не реализовано
    }

    // Метод для проверки наличия функций у учетной записи
    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        return Bundle().apply {
            putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false) // Возвращаем false, если функции отсутствуют
        }
    }
}
