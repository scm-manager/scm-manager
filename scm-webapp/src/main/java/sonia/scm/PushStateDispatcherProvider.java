package sonia.scm;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import javax.inject.Provider;

/**
 * Injection Provider for the {@link PushStateDispatcher}. The provider will return a {@link ProxyPushStateDispatcher}
 * if the system property {@code PushStateDispatcherProvider#PROPERTY_TARGET} is set to a proxy target url, otherwise
 * a {@link ForwardingPushStateDispatcher} is used.
 *
 * @since 2.0.0
 */
public class PushStateDispatcherProvider implements Provider<PushStateDispatcher> {

  @VisibleForTesting
  static final String PROPERTY_TARGET = "sonia.scm.ui.proxy";

  @Override
  public PushStateDispatcher get() {
    String target = System.getProperty(PROPERTY_TARGET);
    if (Strings.isNullOrEmpty(target)) {
      return new ForwardingPushStateDispatcher();
    }
    return new ProxyPushStateDispatcher(target);
  }
}
