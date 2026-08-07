package com.sol.feature.send.impl;

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
public final class SendViewModel_Factory implements Factory<SendViewModel> {
  private final Provider<WalletSessionManager> sessionManagerProvider;

  private SendViewModel_Factory(Provider<WalletSessionManager> sessionManagerProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public SendViewModel get() {
    return newInstance(sessionManagerProvider.get());
  }

  public static SendViewModel_Factory create(
      Provider<WalletSessionManager> sessionManagerProvider) {
    return new SendViewModel_Factory(sessionManagerProvider);
  }

  public static SendViewModel newInstance(WalletSessionManager sessionManager) {
    return new SendViewModel(sessionManager);
  }
}
