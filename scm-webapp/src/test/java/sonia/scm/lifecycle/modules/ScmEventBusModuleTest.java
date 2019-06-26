package sonia.scm.lifecycle.modules;

import com.github.legman.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import sonia.scm.event.LegmanScmEventBus;

import static org.assertj.core.api.Assertions.assertThat;

class ScmEventBusModuleTest {

  @Test
  void shouldRegisterInstance() {
    LegmanScmEventBus eventBus = new LegmanScmEventBus();

    Injector injector = Guice.createInjector(new ScmEventBusModule(eventBus));
    Listener listener = injector.getInstance(Listener.class);

    eventBus.post("hello");

    assertThat(listener.message).isEqualTo("hello");
  }

  public static class Listener {

    private String message;

    @Subscribe(async = false)
    public void receive(String message) {
      this.message = message;
    }

  }
}
