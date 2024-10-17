package com.nur.uss.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MyAccountAuthenticatorService : Service() {

    // Ленивая инициализация экземпляра MyAccountAuthenticator
    private val authenticator: MyAccountAuthenticator by lazy {
        MyAccountAuthenticator(this) // Создаем экземпляр аутентификатора, передавая контекст сервиса
    }

    // Метод вызывается при связывании с сервисом
    override fun onBind(intent: Intent): IBinder {
        // Логируем событие связывания сервиса
        Log.d("MyAccountAuthenticatorService", "Service bound")

        // Возвращаем IBinder для взаимодействия с MyAccountAuthenticator
        return authenticator.iBinder
    }
}
