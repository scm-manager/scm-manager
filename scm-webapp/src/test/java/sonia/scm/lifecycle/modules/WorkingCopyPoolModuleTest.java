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

package sonia.scm.lifecycle.modules;

import com.google.inject.Binder;
import com.google.inject.binder.AnnotatedBindingBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.WebappConfigProvider;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.SimpleWorkingCopyFactory;
import sonia.scm.repository.work.WorkingCopy;
import sonia.scm.repository.work.WorkingCopyPool;

import java.io.File;
import java.util.Map;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkingCopyPoolModuleTest {

  @Mock
  PluginLoader pluginLoader;
  @Mock
  Binder binder;
  @Mock
  AnnotatedBindingBuilder<WorkingCopyPool> bindingBuilder;

  @BeforeEach
  void initClassLoader() {
    lenient().when(pluginLoader.getUberClassLoader()).thenReturn(getClass().getClassLoader());
  }


  @Test
  void shouldBindToDefaultWithoutProperty() {
    WebappConfigProvider.setConfigBindings(Map.of("", ""));
    WorkingCopyPoolModule module = new WorkingCopyPoolModule(pluginLoader);
    when(binder.bind(WorkingCopyPool.class)).thenReturn(bindingBuilder);

    module.configure(binder);

    verify(bindingBuilder).to(NoneCachingWorkingCopyPool.class);
  }

  @Test
  void shouldBindToCustomPoolFromProperty() {
    WebappConfigProvider.setConfigBindings(Map.of("workingCopyPoolStrategy", TestPool.class.getName()));
    WorkingCopyPoolModule module = new WorkingCopyPoolModule(pluginLoader);
    when(binder.bind(WorkingCopyPool.class)).thenReturn(bindingBuilder);

    module.configure(binder);

    verify(bindingBuilder).to(TestPool.class);
  }

  static class TestPool implements WorkingCopyPool {
    @Override
    public <R, W> WorkingCopy<R, W> getWorkingCopy(SimpleWorkingCopyFactory<R, W, ?>.WorkingCopyContext context) {
      return null;
    }

    @Override
    public void contextClosed(SimpleWorkingCopyFactory<?, ?, ?>.WorkingCopyContext workingCopyContext, File workdir) {

    }

    @Override
    public void shutdown() {

    }
  }
}
