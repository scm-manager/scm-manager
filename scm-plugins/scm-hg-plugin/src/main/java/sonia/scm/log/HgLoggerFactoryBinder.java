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

package sonia.scm.log;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.javahg.log.LoggerFactory;
import sonia.scm.plugin.Extension;

@Extension
public class HgLoggerFactoryBinder implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    // Set our own LoggerFactory to prevent usage of javahg fallback JULLogger
    System.setProperty(LoggerFactory.class.getName(), HgLoggerFactory.class.getName());
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    // Do nothing
  }
}
