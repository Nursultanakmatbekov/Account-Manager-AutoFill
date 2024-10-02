package com.nur.uss.utils

import android.accounts.Account

interface AccountHelper {

    fun getAccountsByType(accountType: String): Array<Account>
    fun addAccount(email: String, password: String)
    fun getPassword(account: Account): String?
}