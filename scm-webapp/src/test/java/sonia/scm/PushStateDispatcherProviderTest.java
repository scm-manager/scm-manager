package sonia.scm;

import com.google.inject.util.Providers;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.template.TemplateEngine;

@RunWith(MockitoJUnitRunner.class)
public class PushStateDispatcherProviderTest {

  @Mock
  private TemplateEngine templateEngine;

  private PushStateDispatcherProvider provider = new PushStateDispatcherProvider(
    Providers.of(new TemplatingPushStateDispatcher(templateEngine))
  );

  @Test
  public void testGetProxyPushStateWithPropertySet() {
    System.setProperty(PushStateDispatcherProvider.PROPERTY_TARGET, "http://localhost:9966");
    PushStateDispatcher dispatcher = provider.get();
    Assertions.assertThat(dispatcher).isInstanceOf(ProxyPushStateDispatcher.class);
  }

  @Test
  public void testGetProxyPushStateWithoutProperty() {
    PushStateDispatcher dispatcher = provider.get();
    Assertions.assertThat(dispatcher).isInstanceOf(TemplatingPushStateDispatcher.class);
  }

  @After
  public void cleanupSystemProperty() {
    System.clearProperty(PushStateDispatcherProvider.PROPERTY_TARGET);
  }

}
