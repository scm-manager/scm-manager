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
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.work.WorkingCopyPool;

public class WorkingCopyPoolModule extends AbstractModule {
  public static final String DEFAULT_WORKING_COPY_POOL_STRATEGY = "sonia.scm.repository.work.NoneCachingWorkingCopyPool";
  public static final String WORKING_COPY_POOL_STRATEGY_PROPERTY = "scm.workingCopyPoolStrategy";
  private final PluginLoader pluginLoader;

  public WorkingCopyPoolModule(PluginLoader pluginLoader) {
    this.pluginLoader = pluginLoader;
  }

  @Override
  protected void configure() {
    String workingCopyPoolStrategy = System.getProperty(WORKING_COPY_POOL_STRATEGY_PROPERTY, DEFAULT_WORKING_COPY_POOL_STRATEGY);
    try {
      Class<? extends WorkingCopyPool> strategyClass = (Class<? extends WorkingCopyPool>) pluginLoader.getUberClassLoader().loadClass(workingCopyPoolStrategy);
      bind(WorkingCopyPool.class).to(strategyClass);
    } catch (Exception e) {
      throw new RuntimeException("could not instantiate class for working copy pool: " + workingCopyPoolStrategy, e);
    }
  }
}
