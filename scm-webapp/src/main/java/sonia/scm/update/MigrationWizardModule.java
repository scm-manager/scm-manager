package sonia.scm.update;

import com.google.inject.servlet.ServletModule;
import sonia.scm.update.repository.XmlRepositoryV1UpdateStep;

import java.util.List;

class MigrationWizardModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/*").with(MigrationWizardServlet.class);
  }
}
