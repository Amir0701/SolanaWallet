package com.sol.core.wallet;

import com.sol.crypto.WalletManager;
import com.sol.network.SolanaRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class WalletSessionManager_Factory implements Factory<WalletSessionManager> {
  private final Provider<WalletManager> walletManagerProvider;

  private final Provider<SolanaRepository.Factory> repositoryFactoryProvider;

  private WalletSessionManager_Factory(Provider<WalletManager> walletManagerProvider,
      Provider<SolanaRepository.Factory> repositoryFactoryProvider) {
    this.walletManagerProvider = walletManagerProvider;
    this.repositoryFactoryProvider = repositoryFactoryProvider;
  }

  @Override
  public WalletSessionManager get() {
    return newInstance(walletManagerProvider.get(), repositoryFactoryProvider.get());
  }

  public static WalletSessionManager_Factory create(Provider<WalletManager> walletManagerProvider,
      Provider<SolanaRepository.Factory> repositoryFactoryProvider) {
    return new WalletSessionManager_Factory(walletManagerProvider, repositoryFactoryProvider);
  }

  public static WalletSessionManager newInstance(WalletManager walletManager,
      SolanaRepository.Factory repositoryFactory) {
    return new WalletSessionManager(walletManager, repositoryFactory);
  }
}
