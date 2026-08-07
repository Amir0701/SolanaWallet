package com.sol.feature.receive.impl;

import com.sol.core.wallet.WalletSessionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ReceiveViewModel_Factory implements Factory<ReceiveViewModel> {
  private final Provider<WalletSessionManager> sessionManagerProvider;

  private ReceiveViewModel_Factory(Provider<WalletSessionManager> sessionManagerProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public ReceiveViewModel get() {
    return newInstance(sessionManagerProvider.get());
  }

  public static ReceiveViewModel_Factory create(
      Provider<WalletSessionManager> sessionManagerProvider) {
    return new ReceiveViewModel_Factory(sessionManagerProvider);
  }

  public static ReceiveViewModel newInstance(WalletSessionManager sessionManager) {
    return new ReceiveViewModel(sessionManager);
  }
}
