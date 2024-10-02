package com.nur.uss.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MyAccountAuthenticatorService : Service() {

    private val authenticator: MyAccountAuthenticator by lazy {
        MyAccountAuthenticator(this)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("MyAccountAuthenticatorService", "Service bound")
        return authenticator.iBinder
    }
}
