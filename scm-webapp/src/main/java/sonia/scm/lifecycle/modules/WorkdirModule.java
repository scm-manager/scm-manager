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
import sonia.scm.repository.util.CacheSupportingWorkdirProvider;

public class WorkdirModule extends AbstractModule {
  public static final String DEFAULT_WORKDIR_CACHE_STRATEGY = "sonia.scm.repository.util.NoneCachingWorkdirProvider";
  public static final String WORKDIR_CACHE_STRATEGY_PROPERTY = "scm.workdirCacheStrategy";
  private final PluginLoader pluginLoader;

  public WorkdirModule(PluginLoader pluginLoader) {
    this.pluginLoader = pluginLoader;
  }

  @Override
  protected void configure() {
    try {
      String workdirCacheStrategy = System.getProperty(WORKDIR_CACHE_STRATEGY_PROPERTY, DEFAULT_WORKDIR_CACHE_STRATEGY);
      Class<? extends CacheSupportingWorkdirProvider> strategyClass = (Class<? extends CacheSupportingWorkdirProvider>) pluginLoader.getUberClassLoader().loadClass(workdirCacheStrategy);
      bind(CacheSupportingWorkdirProvider.class).to(strategyClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
