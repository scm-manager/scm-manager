package sonia.scm.repository.update;

import com.google.inject.Injector;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MigrationStrategyMock {

  static Injector init() {
    Map<Class, MigrationStrategy.Instance> mocks = new HashMap<>();
    Injector mock = mock(Injector.class);
    when(
      mock.getInstance(any(Class.class)))
      .thenAnswer(
        invocationOnMock -> mocks.getOrDefault(invocationOnMock.getArgument(0), mock(invocationOnMock.getArgument(0)))
      );
    return mock;
  }
}
