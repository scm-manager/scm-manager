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

import org.apache.shiro.authc.credential.PasswordService;
import sonia.scm.SCMContext;
import sonia.scm.plugin.Extension;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import javax.inject.Inject;
import java.util.Collections;

@Extension
public class AdminAccountStartupAction implements PrivilegedStartupAction {

  private final PasswordService passwordService;
  private final UserManager userManager;
  private final PermissionAssigner permissionAssigner;

  @Inject
  public AdminAccountStartupAction(PasswordService passwordService, UserManager userManager, PermissionAssigner permissionAssigner) {
    this.passwordService = passwordService;
    this.userManager = userManager;
    this.permissionAssigner = permissionAssigner;
  }

  @Override
  public void run() {
    if (shouldCreateAdminAccount()) {
      createAdminAccount();
    }
  }

  private void createAdminAccount() {
    User scmadmin = new User("scmadmin", "SCM Administrator", "scm-admin@scm-manager.org");
    String password = passwordService.encryptPassword("scmadmin");
    scmadmin.setPassword(password);
    userManager.create(scmadmin);

    PermissionDescriptor descriptor = new PermissionDescriptor("*");
    permissionAssigner.setPermissionsForUser("scmadmin", Collections.singleton(descriptor));
  }

  private boolean shouldCreateAdminAccount() {
    return !Boolean.getBoolean("sonia.scm.skipAdminCreation") && (userManager.getAll().isEmpty() || onlyAnonymousUserExists());
  }

  private boolean onlyAnonymousUserExists() {
    return userManager.getAll().size() == 1 && userManager.contains(SCMContext.USER_ANONYMOUS);
  }
}
