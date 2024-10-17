package com.nur.uss.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

class AccountManagerHelper(private val context: Context) {
    // Получаем экземпляр AccountManager для управления учетными записями
    private val accountManager: AccountManager = AccountManager.get(context)
    val keyStoreManager = KeyStoreManager() // Экземпляр KeyStoreManager для работы с шифрованием

    // Метод для получения всех учетных записей заданного типа
    fun getAccountsByType(accountType: String): Array<Account> {
        // Возвращаем массив учетных записей указанного типа
        return accountManager.getAccountsByType(accountType)
    }

    // Метод для добавления новой учетной записи
    fun addAccount(email: String, token: String) {
        // Создаем новую учетную запись с заданным email и типом
        val account = Account(email, "com.nur.uss.account")
        // Добавляем учетную запись, указывая токен в качестве пароля
        accountManager.addAccountExplicitly(account, token, null)
    }

    // Метод для получения токена (пароля) для заданной учетной записи
    fun getToken(account: Account): String? {
        // Возвращаем пароль для указанной учетной записи
        return accountManager.getPassword(account)
    }

    // Метод для сохранения токена (пароля) для заданной учетной записи
    fun saveToken(account: Account, token: String) {
        // Устанавливаем пароль для указанной учетной записи
        accountManager.setPassword(account, token)
    }
}
