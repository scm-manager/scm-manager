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
import com.google.inject.multibindings.Multibinder;
import sonia.scm.migration.NamespaceUpdateStep;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.PluginLoader;

public class UpdateStepModule extends AbstractModule {

  private final PluginLoader pluginLoader;

  public UpdateStepModule(PluginLoader pluginLoader) {
    this.pluginLoader = pluginLoader;
  }

  @Override
  protected void configure() {
    Multibinder<UpdateStep> updateStepBinder = Multibinder.newSetBinder(binder(), UpdateStep.class);
    Multibinder<RepositoryUpdateStep> repositoryUpdateStepBinder = Multibinder.newSetBinder(binder(), RepositoryUpdateStep.class);
    Multibinder<NamespaceUpdateStep> namespaceUpdateStepBinder = Multibinder.newSetBinder(binder(), NamespaceUpdateStep.class);
    pluginLoader
      .getExtensionProcessor()
      .byExtensionPoint(UpdateStep.class)
      .forEach(stepClass -> updateStepBinder.addBinding().to(stepClass));
    pluginLoader
      .getExtensionProcessor()
      .byExtensionPoint(RepositoryUpdateStep.class)
      .forEach(stepClass -> repositoryUpdateStepBinder.addBinding().to(stepClass));
    pluginLoader
      .getExtensionProcessor()
      .byExtensionPoint(NamespaceUpdateStep.class)
      .forEach(stepClass -> namespaceUpdateStepBinder.addBinding().to(stepClass));
  }
}
