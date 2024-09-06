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

package sonia.scm.lifecycle;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import sonia.scm.plugin.Extension;
import sonia.scm.web.security.AdministrationContext;

import java.util.Set;

@Extension
public class SetupContextListener implements ServletContextListener {

  private final Set<PrivilegedStartupAction> startupActions;
  private final AdministrationContext administrationContext;

  @Inject
  public SetupContextListener(Set<PrivilegedStartupAction> startupActions, AdministrationContext administrationContext) {
    this.startupActions = startupActions;
    this.administrationContext = administrationContext;
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    startupActions.forEach(administrationContext::runAsAdmin);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
  }
}
