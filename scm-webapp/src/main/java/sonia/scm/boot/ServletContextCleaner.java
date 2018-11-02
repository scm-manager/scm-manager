package sonia.scm.boot;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Set;

/**
 * Remove cached resources from {@link ServletContext} to allow a clean restart of scm-manager without stale or
 * duplicated data.
 *
 * @since 2.0.0
 */
final class ServletContextCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(ServletContextCleaner.class);

  private static final Set<String> REMOVE_PREFIX = ImmutableSet.of(
    "org.jboss.resteasy",
    "resteasy",
    "org.apache.shiro",
    "sonia.scm"
  );

  private ServletContextCleaner() {
  }

  /**
   * Remove cached attributes from {@link ServletContext}.
   *
   * @param servletContext servlet context
   */
  static void cleanup(ServletContext servletContext) {
    LOG.info("remove cached attributes from context");

    Enumeration<String> attributeNames = servletContext.getAttributeNames();
    while( attributeNames.hasMoreElements()) {
      String name = attributeNames.nextElement();
      if (shouldRemove(name)) {
        LOG.info("remove attribute {} from servlet context", name);
        servletContext.removeAttribute(name);
      } else {
        LOG.info("keep attribute {} in servlet context", name);
      }
    }
  }

  private static boolean shouldRemove(String name) {
    for (String prefix : REMOVE_PREFIX) {
      if (name.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
