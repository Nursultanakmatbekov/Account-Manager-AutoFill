package com.nur.uss.di

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.nur.uss.utils.AccountManagerHelper
import com.nur.uss.utils.KeyStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        // Добавлен метод для получения экземпляра FirebaseAuth
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideAccountManagerHelper(application: Application): AccountManagerHelper {
        // Добавлен метод для получения экземпляра AccountManagerHelper
        return AccountManagerHelper(application)
    }

    @Provides
    @Singleton
    fun provideKeyStoreManager(): KeyStoreManager {
        // Добавлен метод для получения единственного экземпляра KeyStoreManager
        return KeyStoreManager()
    }
}
