package com.nur.uss.di

import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.nur.uss.utils.AccountManagerHelper
import com.nur.uss.utils.KeyStoreManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideAccountManagerHelper(application: Application): AccountManagerHelper {
        return AccountManagerHelper(application)
    }
    @Provides
    @Singleton
    fun provideKeyStoreManager(): KeyStoreManager {
        return KeyStoreManager()
    }
}
