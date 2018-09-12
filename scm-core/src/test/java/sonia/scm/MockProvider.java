package sonia.scm;

import javax.inject.Provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A mockito implementation of CDI {@link javax.inject.Provider}.
 */
public class MockProvider {

  private MockProvider() {}

  public static <I> Provider<I> of(I instance) {
    @SuppressWarnings("unchecked") // Can't make mockito return typed provider
    Provider<I> provider = mock(Provider.class);
    when(provider.get()).thenReturn(instance);
    return provider;
  }

}
