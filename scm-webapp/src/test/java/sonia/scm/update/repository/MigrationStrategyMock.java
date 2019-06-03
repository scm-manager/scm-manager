package sonia.scm.update.repository;

import com.google.inject.Injector;
import sonia.scm.update.repository.MigrationStrategy.Instance;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MigrationStrategyMock {

  static Injector init() {
    Map<Class, Instance> mocks = new HashMap<>();
    Injector mock = mock(Injector.class);
    when(
      mock.getInstance(any(Class.class)))
      .thenAnswer(
        invocationOnMock -> mocks.computeIfAbsent(invocationOnMock.getArgument(0), key -> mock((Class<Instance>) key))
      );
    return mock;
  }
}
