package sonia.scm.update;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import sonia.scm.update.repository.XmlRepositoryV1UpdateStep;

public class MigrationWizardContextListener extends GuiceServletContextListener {

  private final Injector bootstrapInjector;

  public MigrationWizardContextListener(Injector bootstrapInjector) {
    this.bootstrapInjector = bootstrapInjector;
  }

  public boolean wizardNecessary() {
    return !bootstrapInjector.getInstance(XmlRepositoryV1UpdateStep.class).getRepositoriesWithoutMigrationStrategies().isEmpty();
  }

  @Override
  protected Injector getInjector() {
    return bootstrapInjector.createChildInjector(new MigrationWizardModule());
  }
}
