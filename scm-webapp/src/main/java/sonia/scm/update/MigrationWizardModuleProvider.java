package sonia.scm.update;

import com.google.inject.Injector;
import com.google.inject.Module;
import sonia.scm.lifecycle.modules.ModuleProvider;
import sonia.scm.update.repository.XmlRepositoryV1UpdateStep;

import java.util.Collection;
import java.util.Collections;

public class MigrationWizardModuleProvider implements ModuleProvider {

  private final Injector bootstrapInjector;

  public MigrationWizardModuleProvider(Injector bootstrapInjector) {
    this.bootstrapInjector = bootstrapInjector;
  }

  public boolean wizardNecessary() {
    return !bootstrapInjector.getInstance(XmlRepositoryV1UpdateStep.class).getRepositoriesWithoutMigrationStrategies().isEmpty();
  }

  @Override
  public Collection<Module> createModules() {
    return Collections.singleton(new MigrationWizardModule());
  }
}
