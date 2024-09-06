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

import com.google.inject.AbstractModule;
import sonia.scm.config.WebappConfigProvider;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkingCopyPool;

public class WorkingCopyPoolModule extends AbstractModule {
  private final ClassLoader classLoader;

  public WorkingCopyPoolModule(PluginLoader pluginLoader) {
    this.classLoader = pluginLoader.getUberClassLoader();
  }

  @Override
  protected void configure() {
    String workingCopyPoolStrategy = WebappConfigProvider.resolveAsString("workingCopyPoolStrategy")
      .orElse(NoneCachingWorkingCopyPool.class.getName());
    try {
      Class<? extends WorkingCopyPool> strategyClass = (Class<? extends WorkingCopyPool>) classLoader.loadClass(workingCopyPoolStrategy);
      bind(WorkingCopyPool.class).to(strategyClass);
    } catch (Exception e) {
      throw new IllegalStateException("could not instantiate class for working copy pool: " + workingCopyPoolStrategy, e);
    }
  }
}
