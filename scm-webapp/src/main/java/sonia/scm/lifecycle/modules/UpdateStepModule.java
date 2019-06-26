package sonia.scm.lifecycle.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
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
    pluginLoader
      .getExtensionProcessor()
      .byExtensionPoint(UpdateStep.class)
      .forEach(stepClass -> updateStepBinder.addBinding().to(stepClass));
  }
}
