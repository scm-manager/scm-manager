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
