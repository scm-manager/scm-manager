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
