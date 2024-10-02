package com.nur.uss.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

class AccountManagerHelper(private val context: Context)  : AccountHelper {
    private val accountManager: AccountManager = AccountManager.get(context)

    override fun getAccountsByType(accountType: String): Array<Account> {
        return accountManager.getAccountsByType(accountType)
    }

    override fun addAccount(email: String, password: String) {
        val account = Account(email, "com.nur.uss.account")
        accountManager.addAccountExplicitly(account, password, null)
    }

    override fun getPassword(account: Account): String? {
        return accountManager.getPassword(account)
    }
}