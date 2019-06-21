package sonia.scm;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.assertj.core.api.Assertions.assertThat;

class EagerSingletonModuleTest {

  @Test
  void shouldInitializeEagerSingletons() {
    Injector injector = Guice.createInjector(new EagerSingletonModule(), new EagerTestModule());
    injector.getInstance(EagerSingletonModule.class).initialize(injector);

    Capturer capturer = injector.getInstance(Capturer.class);
    assertThat(capturer.value).isEqualTo("eager!");
  }

  public static class EagerTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(Capturer.class);
      bind(Eager.class);
    }
  }

  @Singleton
  public static class Capturer {
    private String value;
  }

  @EagerSingleton
  public static class Eager {

    @Inject
    public Eager(Capturer capturer) {
      capturer.value = "eager!";
    }
  }

}
