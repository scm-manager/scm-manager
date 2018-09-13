package sonia.scm;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Injection Provider for the {@link PushStateDispatcher}. The provider will return a {@link ProxyPushStateDispatcher}
 * if the system property {@code PushStateDispatcherProvider#PROPERTY_TARGET} is set to a proxy target url, otherwise
 * a {@link TemplatingPushStateDispatcher} is used.
 *
 * @since 2.0.0
 */
public class PushStateDispatcherProvider implements Provider<PushStateDispatcher> {

  @VisibleForTesting
  static final String PROPERTY_TARGET = "sonia.scm.ui.proxy";

  private Provider<TemplatingPushStateDispatcher> templatingPushStateDispatcherProvider;

  @Inject
  public PushStateDispatcherProvider(Provider<TemplatingPushStateDispatcher> templatingPushStateDispatcherProvider) {
    this.templatingPushStateDispatcherProvider = templatingPushStateDispatcherProvider;
  }

  @Override
  public PushStateDispatcher get() {
    String target = System.getProperty(PROPERTY_TARGET);
    if (Strings.isNullOrEmpty(target)) {
      return templatingPushStateDispatcherProvider.get();
    }
    return new ProxyPushStateDispatcher(target);
  }
}
