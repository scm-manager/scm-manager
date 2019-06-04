package sonia.scm.update;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class MigrationWizardContextListener extends GuiceServletContextListener {

  private final Injector injector;

  public MigrationWizardContextListener(Injector bootstrapInjector) {
    this.injector = bootstrapInjector.createChildInjector(new MigrationWizardModule());
  }

  public boolean wizardNecessary() {
    return injector.getInstance(MigrationWizardServlet.class).wizardNecessary();
  }

  @Override
  protected Injector getInjector() {
    return injector;
  }
}
