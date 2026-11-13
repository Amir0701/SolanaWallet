package com.sol.feature.send.impl.di

import com.sol.feature.send.impl.data.TransferRepositoryImpl
import com.sol.feature.send.impl.domain.repository.TransferRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class SendModule {
    @Binds
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository
}
