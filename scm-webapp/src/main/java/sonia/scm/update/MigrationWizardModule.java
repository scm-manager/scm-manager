package sonia.scm.update;

import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.PushStateDispatcher;
import sonia.scm.WebResourceServlet;

class MigrationWizardModule extends ServletModule {

  private static final Logger LOG = LoggerFactory.getLogger(MigrationWizardModule.class);

  @Override
  protected void configureServlets() {
    LOG.info("==========================================================");
    LOG.info("=                                                        =");
    LOG.info("=             STARTING MIGRATION SERVLET                 =");
    LOG.info("=                                                        =");
    LOG.info("==========================================================");
    bind(PushStateDispatcher.class).toInstance((request, response, uri) -> {});
    serve("/images/*", "/styles/*").with(WebResourceServlet.class);
    serve("/*").with(MigrationWizardServlet.class);
  }
}
