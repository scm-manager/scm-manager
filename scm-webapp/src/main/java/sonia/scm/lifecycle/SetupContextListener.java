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

import com.google.common.annotations.VisibleForTesting;
import org.apache.shiro.authc.credential.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AnonymousMode;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Collections;

import static sonia.scm.group.GroupCollector.AUTHENTICATED;

@Extension
public class SetupContextListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(SetupContextListener.class);

  private final AdministrationContext administrationContext;

  @Inject
  public SetupContextListener(AdministrationContext administrationContext) {
    this.administrationContext = administrationContext;
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    if (Boolean.getBoolean("sonia.scm.skipAdminCreation")) {
      LOG.info("found skipAdminCreation flag; skipping creation of scmadmin");
    } else {
      administrationContext.runAsAdmin(SetupAction.class);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}

  @VisibleForTesting
  static class SetupAction implements PrivilegedAction {

    private final UserManager userManager;
    private final PasswordService passwordService;
    private final PermissionAssigner permissionAssigner;
    private final ScmConfiguration scmConfiguration;
    private final GroupManager groupManager;

    private static final String AUTHENTICATED_GROUP_DESCRIPTION = "Includes all authenticated users";

    @Inject
    public SetupAction(UserManager userManager, PasswordService passwordService, PermissionAssigner permissionAssigner, ScmConfiguration scmConfiguration, GroupManager groupManager) {
      this.userManager = userManager;
      this.passwordService = passwordService;
      this.permissionAssigner = permissionAssigner;
      this.scmConfiguration = scmConfiguration;
      this.groupManager = groupManager;
    }

    @Override
    public void run() {
      if (shouldCreateAdminAccount()) {
        createAdminAccount();
      }
      if (anonymousUserRequiredButNotExists()) {
        userManager.create(SCMContext.ANONYMOUS);
      }

      if (authenticatedGroupDoesNotExists()) {
        createAuthenticatedGroup();
      }
    }

    private boolean anonymousUserRequiredButNotExists() {
      return scmConfiguration.getAnonymousMode() != AnonymousMode.OFF && !userManager.contains(SCMContext.USER_ANONYMOUS);
    }

    private boolean shouldCreateAdminAccount() {
      return userManager.getAll().isEmpty() || onlyAnonymousUserExists();
    }

    private boolean onlyAnonymousUserExists() {
      return userManager.getAll().size() == 1 && userManager.contains(SCMContext.USER_ANONYMOUS);
    }

    private void createAdminAccount() {
      User scmadmin = new User("scmadmin", "SCM Administrator", "scm-admin@scm-manager.org");
      String password = passwordService.encryptPassword("scmadmin");
      scmadmin.setPassword(password);
      userManager.create(scmadmin);

      PermissionDescriptor descriptor = new PermissionDescriptor("*");
      permissionAssigner.setPermissionsForUser("scmadmin", Collections.singleton(descriptor));
    }

    private boolean authenticatedGroupDoesNotExists() {
      return groupManager == null || groupManager.get(AUTHENTICATED) == null;
    }

    private void createAuthenticatedGroup() {
      Group authenticated = new Group("xml", AUTHENTICATED);
      authenticated.setDescription(AUTHENTICATED_GROUP_DESCRIPTION);
      groupManager.create(authenticated);
    }
  }
}
