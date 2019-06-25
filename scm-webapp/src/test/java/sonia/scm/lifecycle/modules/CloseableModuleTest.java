package sonia.scm.lifecycle.modules;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;

import java.io.Closeable;

import static org.assertj.core.api.Assertions.assertThat;

class CloseableModuleTest {

  @Test
  void shouldCloseCloseables() {
    Injector injector = Guice.createInjector(new CloseableModule());
    CloseMe closeMe = injector.getInstance(CloseMe.class);

    injector.getInstance(CloseableModule.class).closeAll();
    assertThat(closeMe.closed).isTrue();
  }

  public static class CloseMe implements Closeable {

    private boolean closed = false;

    @Override
    public void close() {
      this.closed = true;
    }
  }

}
