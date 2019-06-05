package sonia.scm.update;

import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MigrationWizardModule extends ServletModule {

  private static final Logger LOG = LoggerFactory.getLogger(MigrationWizardModule.class);

  @Override
  protected void configureServlets() {
    LOG.info("==========================================================");
    LOG.info("=                                                        =");
    LOG.info("=             STARTING MIGRATION SERVLET                 =");
    LOG.info("=                                                        =");
    LOG.info("==========================================================");
    serve("/*").with(MigrationWizardServlet.class);
  }
}
