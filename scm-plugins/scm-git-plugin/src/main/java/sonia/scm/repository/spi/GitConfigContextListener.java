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
    
package sonia.scm.repository.spi;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.eclipse.jgit.util.SystemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

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
