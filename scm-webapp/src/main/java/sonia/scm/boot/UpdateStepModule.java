package sonia.scm.boot;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.PluginLoader;

class UpdateStepModule extends AbstractModule {

  private final PluginLoader pluginLoader;

  UpdateStepModule(PluginLoader pluginLoader) {
    this.pluginLoader = pluginLoader;
  }

  @Override
  protected void configure() {
    Multibinder<UpdateStep> updateStepBinder = Multibinder.newSetBinder(binder(), UpdateStep.class);
    pluginLoader
      .getExtensionProcessor()
      .byExtensionPoint(UpdateStep.class)
      .forEach(stepClass -> updateStepBinder.addBinding().to(stepClass));
  }
}
