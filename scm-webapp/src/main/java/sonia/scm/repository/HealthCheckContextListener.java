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
