package sonia.scm.update;

import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.StaticResourceServlet;

class MigrationWizardModule extends ServletModule {

  private static final Logger LOG = LoggerFactory.getLogger(MigrationWizardModule.class);

  @Override
  protected void configureServlets() {
    LOG.info("==========================================================");
    LOG.info("=                                                        =");
    LOG.info("=             STARTING MIGRATION SERVLET                 =");
    LOG.info("=                                                        =");
    LOG.info("=   Open SCM-Manager in a browser to start the wizard.   =");
    LOG.info("=                                                        =");
    LOG.info("==========================================================");
    serve("/images/*", "/assets/*", "/favicon.ico").with(StaticResourceServlet.class);
    serve("/*").with(MigrationWizardServlet.class);
  }
}
