/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
