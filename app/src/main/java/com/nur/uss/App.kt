package com.nur.uss

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    // Это основной класс приложения, аннотированный @HiltAndroidApp для интеграции Dagger Hilt.
}
