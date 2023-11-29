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
