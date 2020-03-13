package sonia.scm.repository.spi;

import org.eclipse.jgit.util.SystemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

@Extension
public class GitConfigContextListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(GitConfigContextListener.class);
  private static final String SCM_JGIT_CORE = "scm.git.core.";

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    System.getProperties()
      .entrySet().stream()
      .filter(e -> e.getKey().toString().startsWith(SCM_JGIT_CORE))
      .forEach(this::setConfig);
  }

  private void setConfig(Map.Entry<Object, Object> property) {
    String key = property.getKey().toString().substring(SCM_JGIT_CORE.length());
    String value = property.getValue().toString();
    try {
      SystemReader.getInstance().getSystemConfig().setString("core", null, key, value);
      LOG.info("set git config core.{} = {}", key,value);
    } catch (Exception e) {
      LOG.error("could not set git config core.{} = {}", key,value, e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // nothing to do
  }
}
