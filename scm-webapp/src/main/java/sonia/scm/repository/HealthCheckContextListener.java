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


import com.google.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.shiro.SecurityUtils;
import sonia.scm.plugin.Extension;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;


@Extension
public class HealthCheckContextListener implements ServletContextListener
{
  private final AdministrationContext context;
 
  @Inject
  public HealthCheckContextListener(AdministrationContext context)
  {
    this.context = context;
  }



  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {

    // do nothing
  }


  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    context.runAsAdmin(HealthCheckStartupAction.class);
  }




  static class HealthCheckStartupAction implements PrivilegedAction
  {
    private final HealthChecker healthChecker;
  
    @Inject
    public HealthCheckStartupAction(HealthChecker healthChecker)
    {
      this.healthChecker = healthChecker;
    }

    


    @Override
    public void run()
    {

      // excute health checks for all repsitories asynchronous
      SecurityUtils.getSubject().execute(healthChecker::lightCheckAll);
    }

  }

}
