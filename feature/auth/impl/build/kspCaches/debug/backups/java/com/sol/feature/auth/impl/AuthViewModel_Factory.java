package com.sol.feature.auth.impl;

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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<WalletSessionManager> sessionManagerProvider;

  private AuthViewModel_Factory(Provider<WalletSessionManager> sessionManagerProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(sessionManagerProvider.get());
  }

  public static AuthViewModel_Factory create(
      Provider<WalletSessionManager> sessionManagerProvider) {
    return new AuthViewModel_Factory(sessionManagerProvider);
  }

  public static AuthViewModel newInstance(WalletSessionManager sessionManager) {
    return new AuthViewModel(sessionManager);
  }
}
