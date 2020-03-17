/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.lifecycle;

import com.github.legman.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.RecreateEventBusEvent;
import sonia.scm.event.ScmEventBus;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InjectionContextRestartStrategyTest {

  @Mock
  private RestartStrategy.InjectionContext context;

  private InjectionContextRestartStrategy strategy = new InjectionContextRestartStrategy(Thread.currentThread().getContextClassLoader());

  @BeforeEach
  void setWaitToZero() {
    strategy.setWaitInMs(0L);
    // disable gc during tests
    strategy.setGcEnabled(false);
  }

  @Test
  void shouldCallDestroyAndInitialize() {
    TestingInjectionContext ctx = new TestingInjectionContext();
    strategy.restart(ctx);
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> assertThat(ctx.destroyed).isTrue());
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> assertThat(ctx.initialized).isTrue());
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

    await().atMost(1, TimeUnit.SECONDS).until(() -> ctx.initialized);
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
    private boolean initialized = false;
    private boolean destroyed = false;

    @Subscribe(async = false)
    public void setEvent(String event) {
      this.event = event;
    }

    @Override
    public void initialize() {
      this.initialized = true;
    }

    @Override
    public void destroy() {
      this.destroyed = true;
    }
  }

}
