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

package sonia.scm.repository;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.eclipse.jgit.transport.HttpTransport;
import sonia.scm.plugin.Extension;
import sonia.scm.web.ScmHttpConnectionFactory;

@Extension
public class GitHttpTransportRegistration implements ServletContextListener {

  private final ScmHttpConnectionFactory connectionFactory;

  @Inject
  public GitHttpTransportRegistration(ScmHttpConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    // Override default http connection factory to inject our own ssl context
    HttpTransport.setConnectionFactory(connectionFactory);
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    // Nothing to destroy
  }

}
