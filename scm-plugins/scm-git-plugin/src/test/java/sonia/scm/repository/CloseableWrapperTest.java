package sonia.scm.repository;

import org.junit.Test;

import java.util.function.Consumer;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class CloseableWrapperTest {

  @Test
  public void x() {
    Consumer<String> wrapped = new Consumer<String>() {
      // no this cannot be replaced with a lambda because otherwise we could not use Mockito#spy
      @Override
      public void accept(String s) {
      }
    };

    Consumer<String> closer = spy(wrapped);

    try (CloseableWrapper<String> wrapper = new CloseableWrapper<>("test", closer)) {
      // nothing to do here
    }

    verify(closer).accept("test");
  }
}
