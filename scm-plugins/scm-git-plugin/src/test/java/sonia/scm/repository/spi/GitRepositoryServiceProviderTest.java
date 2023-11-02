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

package sonia.scm.repository.spi;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.GitRepositoryHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GitRepositoryServiceProviderTest {

  @Mock
  private GitRepositoryHandler handler;

  @Mock
  private GitContext context;

  @Test
  void shouldCreatePushCommand() {
    GitRepositoryServiceProvider provider = createProvider();
    PushCommand pushCommand = provider.getPushCommand();
    assertThat(pushCommand).isNotNull().isInstanceOf(GitPushCommand.class);
  }

  @Test
  void shouldDelegateCloseToContext() {
    createProvider().close();
    verify(context).close();
  }

  private GitRepositoryServiceProvider createProvider() {

    return new GitRepositoryServiceProvider(createParentInjector(), context);
  }

  private Injector createParentInjector() {
    return Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(GitRepositoryHandler.class).toInstance(handler);
        FactoryModuleBuilder builder = new FactoryModuleBuilder();
        Module module = builder.implement(PushCommand.class, GitPushCommand.class).build(GitPushCommand.Factory.class);
        install(module);
      }
    });
  }

}
