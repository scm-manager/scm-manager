package sonia.scm.repository;

import org.eclipse.jgit.lib.internal.WorkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Extension
public class GitWorkQueueShutdownListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(GitWorkQueueShutdownListener.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {

  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    LOG.warn("shutdown jGit WorkQueue executor");
    WorkQueue.getExecutor().shutdown();
  }
}
