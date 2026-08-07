package com.sol.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_wallet_storage",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun saveMnemonic(words: List<String>) {
        prefs.edit()
            .putString(KEY_MNEMONIC, words.joinToString(" "))
            .apply()
    }

    fun getMnemonic(): List<String>? =
        prefs.getString(KEY_MNEMONIC, null)?.trim()?.split("\\s+".toRegex())?.filter { it.isNotEmpty() }

    fun clearMnemonic() {
        prefs.edit().remove(KEY_MNEMONIC).apply()
    }

    companion object {
        private const val KEY_MNEMONIC = "mnemonic"
    }
}
