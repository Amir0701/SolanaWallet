package com.sol.cryprowallet.di

import android.content.Context
import com.sol.crypto.SecureStorage
import com.sol.crypto.WalletManager
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
    @Singleton
    fun provideSecureStorage(
        @ApplicationContext context: Context,
    ): SecureStorage = SecureStorage(context)

    @Provides
    @Singleton
    fun provideWalletManager(
        secureStorage: SecureStorage,
    ): WalletManager = WalletManager(secureStorage)
}

