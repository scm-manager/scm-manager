/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.lifecycle;

import com.google.common.collect.ImmutableSet;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
