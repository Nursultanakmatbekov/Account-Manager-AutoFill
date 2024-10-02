package com.nur.uss.utils

interface EncryptionManager {

    fun encrypt(data: String): String
    fun decrypt(data: String): String
}