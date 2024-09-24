/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
