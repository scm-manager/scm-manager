package sonia.scm.lifecycle.modules;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import sonia.scm.Initable;
import sonia.scm.SCMContextProvider;

import static org.assertj.core.api.Assertions.assertThat;

class ScmInitializerModuleTest {

  @Test
  void shouldInitializeInstances() {
    Injector injector = Guice.createInjector(new ScmInitializerModule());
    InitializeMe instance = injector.getInstance(InitializeMe.class);

    assertThat(instance.initialized).isTrue();
  }

  public static class InitializeMe implements Initable {

    private boolean initialized = false;

    @Override
    public void init(SCMContextProvider context) {
      this.initialized = true;
    }
  }

}
