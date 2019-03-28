package sonia.scm.repository;

import org.junit.Test;
import sonia.scm.repository.util.CloseableWrapper;

import java.util.function.Consumer;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class CloseableWrapperTest {

  @Test
  public void shouldExecuteGivenMethodAtClose() {
    Consumer<AutoCloseable> wrapped = new Consumer<AutoCloseable>() {
      // no this cannot be replaced with a lambda because otherwise we could not use Mockito#spy
      @Override
      public void accept(AutoCloseable s) {
      }
    };

    Consumer<AutoCloseable> closer = spy(wrapped);

    AutoCloseable autoCloseable = () -> {};
    try (CloseableWrapper<AutoCloseable> wrapper = new CloseableWrapper<>(autoCloseable, closer)) {
      // nothing to do here
    }

    verify(closer).accept(autoCloseable);
  }
}
