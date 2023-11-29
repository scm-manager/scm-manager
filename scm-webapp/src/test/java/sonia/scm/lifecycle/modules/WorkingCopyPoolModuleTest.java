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
