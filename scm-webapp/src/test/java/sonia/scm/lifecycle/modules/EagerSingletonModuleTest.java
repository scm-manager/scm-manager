/*
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
    
package sonia.scm.lifecycle.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import sonia.scm.EagerSingleton;

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
