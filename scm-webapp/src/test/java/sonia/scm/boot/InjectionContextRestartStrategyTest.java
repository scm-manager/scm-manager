package sonia.scm.boot;

import com.github.legman.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.RecreateEventBusEvent;
import sonia.scm.event.ScmEventBus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InjectionContextRestartStrategyTest {

  @Mock
  private RestartStrategy.InjectionContext context;

  private InjectionContextRestartStrategy strategy = new InjectionContextRestartStrategy();

  @BeforeEach
  void setWaitToZero() {
    strategy.setWaitInMs(0L);
  }

  @Test
  void shouldCallDestroyAndInitialize() throws InterruptedException {
    strategy.restart(context);

    verify(context).destroy();
    Thread.sleep(50L);
    verify(context).initialize();
  }

  @Test
  void shouldFireRecreateEventBusEvent() {
    Listener listener = new Listener();
    ScmEventBus.getInstance().register(listener);

    strategy.restart(context);

    assertThat(listener.event).isNotNull();
  }

  @Test
  void shouldRegisterContextAfterRestart() throws InterruptedException {
    TestingInjectionContext ctx = new TestingInjectionContext();

    strategy.restart(ctx);

    Thread.sleep(50L);
    ScmEventBus.getInstance().post("hello event");

    assertThat(ctx.event).isEqualTo("hello event");
  }

  public static class Listener {

    private RecreateEventBusEvent event;

    @Subscribe(async = false)
    public void setEvent(RecreateEventBusEvent event) {
      this.event = event;
    }
  }

  public static class TestingInjectionContext implements RestartStrategy.InjectionContext {

    private volatile String event;

    @Subscribe(async = false)
    public void setEvent(String event) {
      this.event = event;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void destroy() {

    }
  }

}
