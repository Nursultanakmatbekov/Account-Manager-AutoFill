package com.nur.uss.utils

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.util.Log

class MyAccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {
    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        return null
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val account = Account("new_account", accountType ?: "com.nur.uss.account")
        val accountManager = AccountManager.get(context)

        val success = accountManager.addAccountExplicitly(account, "password", null)
        Log.d("MyAccountAuthenticator", "Attempting to add account: $accountType")
        return if (success) {
            Log.d("MyAccountAuthenticator", "Account added: ${account.name}")
            Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            }
        } else {
            Log.e("MyAccountAuthenticator", "Failed to add account: ${account.name}")
            Bundle().apply {
                putString(AccountManager.KEY_ERROR_CODE, "1")
                putString(AccountManager.KEY_ERROR_MESSAGE, "Failed to add account.")
            }
        }
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun getAuthTokenLabel(authTokenType: String): String {
        return authTokenType
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        return Bundle().apply {
            putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        }
    }
}
