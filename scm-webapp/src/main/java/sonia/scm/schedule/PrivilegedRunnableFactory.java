package sonia.scm.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.web.security.AdministrationContext;

import javax.inject.Inject;
import javax.inject.Provider;

class PrivilegedRunnableFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PrivilegedRunnableFactory.class);

  private final AdministrationContext context;

  @Inject
  PrivilegedRunnableFactory(AdministrationContext context) {
    this.context = context;
  }

  public Runnable create(Provider<? extends Runnable> runnableProvider) {
    return () -> context.runAsAdmin(() -> {
      LOG.trace("create runnable from provider");
      Runnable runnable = runnableProvider.get();
      LOG.debug("execute scheduled job {}", runnable.getClass());
      runnable.run();
    });
  }
}
