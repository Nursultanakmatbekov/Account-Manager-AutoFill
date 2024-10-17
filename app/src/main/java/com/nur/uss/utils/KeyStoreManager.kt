package com.nur.uss.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeyStoreManager {

    // Получаем экземпляр хранилища ключей
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "myKeyAlias" // Псевдоним для ключа

    init {
        generateKey() // Генерируем ключ при инициализации
    }

    // Метод для генерации ключа
    private fun generateKey() {
        // Проверяем, существует ли ключ с данным псевдонимом
        if (!keyStore.containsAlias(keyAlias)) {
            // Создаем генератор ключей для алгоритма AES
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            // Настраиваем параметры генерации ключа
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                // Указываем режимы работы ключа
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keyGenParameterSpec) // Инициализируем генератор с заданными параметрами
            keyGenerator.generateKey() // Генерируем ключ
            Log.d("KeyStoreManager", "Key generated successfully.")
        } else {
            Log.d("KeyStoreManager", "Key already exists.")
        }
    }

    // Метод для шифрования данных
    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding") // Создаем экземпляр шифра
        cipher.init(Cipher.ENCRYPT_MODE, getKey()) // Инициализируем шифр для шифрования
        val iv = cipher.iv // Получаем вектор инициализации (IV)
        val encryptedData = cipher.doFinal(data.toByteArray()) // Шифруем данные

        // Объединяем IV и зашифрованные данные
        val combined = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, combined, 0, iv.size) // Копируем IV в общий массив
        System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size) // Копируем зашифрованные данные

        return Base64.encodeToString(combined, Base64.DEFAULT) // Кодируем результат в строку Base64
    }

    // Метод для расшифровки данных
    fun decrypt(data: String): String {
        val encryptedData = Base64.decode(data, Base64.DEFAULT) // Декодируем строку Base64
        val iv = encryptedData.copyOfRange(0, 12) // Извлекаем IV
        val encryptedBytes = encryptedData.copyOfRange(12, encryptedData.size) // Извлекаем зашифрованные данные
        val cipher = Cipher.getInstance("AES/GCM/NoPadding") // Создаем экземпляр шифра
        cipher.init(
            Cipher.DECRYPT_MODE,
            getKey(), // Инициализируем шифр для расшифровки
            GCMParameterSpec(128, iv) // Указываем параметры GCM с длиной блока 128 бит
        )
        val decryptedData = cipher.doFinal(encryptedBytes) // Расшифровываем данные
        return String(decryptedData) // Преобразуем результат в строку
    }

    // Метод для получения ключа из Keystore
    private fun getKey(): SecretKey {
        val key = keyStore.getKey(keyAlias, null) // Получаем ключ по псевдониму
        return when (key) {
            is SecretKey -> key // Возвращаем ключ, если он типа SecretKey
            else -> throw IllegalStateException("Key not found or is not a SecretKey") // Исключение, если ключ не найден или не является SecretKey
        }
    }
}
