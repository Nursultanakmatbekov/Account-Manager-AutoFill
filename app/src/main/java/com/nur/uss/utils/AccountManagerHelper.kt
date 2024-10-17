package com.nur.uss.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

class AccountManagerHelper(private val context: Context) {
    private val accountManager: AccountManager = AccountManager.get(context)
    val keyStoreManager = KeyStoreManager()

    fun getAccountsByType(accountType: String): Array<Account> {
        return accountManager.getAccountsByType(accountType)
    }

    fun addAccount(email: String, token: String) {
        val account = Account(email, "com.nur.uss.account")
        accountManager.addAccountExplicitly(account, token, null)
    }

    fun getToken(account: Account): String? {
        return accountManager.getPassword(account)
    }

    fun saveToken(account: Account, token: String) {
        accountManager.setPassword(account, token)
    }
}
