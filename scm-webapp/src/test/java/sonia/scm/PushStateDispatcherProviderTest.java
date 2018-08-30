package sonia.scm;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class PushStateDispatcherProviderTest {

  private PushStateDispatcherProvider provider = new PushStateDispatcherProvider();

  @Test
  public void testGetProxyPushStateWithPropertySet() {
    System.setProperty(PushStateDispatcherProvider.PROPERTY_TARGET, "http://localhost:9966");
    PushStateDispatcher dispatcher = provider.get();
    Assertions.assertThat(dispatcher).isInstanceOf(ProxyPushStateDispatcher.class);
  }

  @Test
  public void testGetProxyPushStateWithoutProperty() {
    PushStateDispatcher dispatcher = provider.get();
    Assertions.assertThat(dispatcher).isInstanceOf(ForwardingPushStateDispatcher.class);
  }

  @After
  public void cleanupSystemProperty() {
    System.clearProperty(PushStateDispatcherProvider.PROPERTY_TARGET);
  }

}
