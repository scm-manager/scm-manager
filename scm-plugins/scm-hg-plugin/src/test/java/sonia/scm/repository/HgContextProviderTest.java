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
package sonia.scm.repository;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Scope;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.util.Providers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgContextProviderTest {

  @Mock
  private Scope scope;

  @Test
  void shouldThrowNonOutOfScopeProvisionExceptions() {
    Provider<HgContextRequestStore> provider = () -> {
      throw new RuntimeException("something different");
    };

    when(scope.scope(any(Key.class), any(Provider.class))).thenReturn(provider);

    Injector injector = Guice.createInjector(new HgContextModule(scope));

    assertThrows(ProvisionException.class, () -> injector.getInstance(HgContext.class));
  }

  @Test
  void shouldCreateANewInstanceIfOutOfRequestScope() {
    Provider<HgContextRequestStore> provider = () -> {
      throw new OutOfScopeException("no request");
    };
    when(scope.scope(any(Key.class), any(Provider.class))).thenReturn(provider);

    Injector injector = Guice.createInjector(new HgContextModule(scope));

    HgContext contextOne = injector.getInstance(HgContext.class);
    HgContext contextTwo = injector.getInstance(HgContext.class);

    assertThat(contextOne).isNotSameAs(contextTwo);
  }

  @Test
  void shouldInjectFromRequestScope() {
    HgContextRequestStore requestStore = new HgContextRequestStore();
    Provider<HgContextRequestStore> provider = Providers.of(requestStore);

    when(scope.scope(any(Key.class), any(Provider.class))).thenReturn(provider);

    Injector injector = Guice.createInjector(new HgContextModule(scope));

    HgContext contextOne = injector.getInstance(HgContext.class);
    HgContext contextTwo = injector.getInstance(HgContext.class);

    assertThat(contextOne).isSameAs(contextTwo);
  }

  private static class HgContextModule extends AbstractModule {

    private Scope scope;

    private HgContextModule(Scope scope) {
      this.scope = scope;
    }

    @Override
    protected void configure() {
      bindScope(RequestScoped.class, scope);
      bind(HgContextRequestStore.class);
      bind(HgContext.class).toProvider(HgContextProvider.class);
    }
  }
}
