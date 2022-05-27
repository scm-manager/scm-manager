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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Extension
@Singleton
public class AdminAccountStartupAction implements InitializationStep {

  private static final Logger LOG = LoggerFactory.getLogger(AdminAccountStartupAction.class);

  public static final String INITIAL_PASSWORD_PROPERTY = "scm.initialPassword";
  private static final String INITIAL_USER_PROPERTY = "scm.initialUser";

  private final PasswordService passwordService;
  private final UserManager userManager;
  private final PermissionAssigner permissionAssigner;
  private final RandomPasswordGenerator randomPasswordGenerator;
  private final AdministrationContext context;

  private String initialToken;

  @Inject
  public AdminAccountStartupAction(PasswordService passwordService, UserManager userManager, PermissionAssigner permissionAssigner, RandomPasswordGenerator randomPasswordGenerator, AdministrationContext context) {
    this.passwordService = passwordService;
    this.userManager = userManager;
    this.permissionAssigner = permissionAssigner;
    this.randomPasswordGenerator = randomPasswordGenerator;
    this.context = context;

    initialize();
  }

  private void initialize() {
    context.runAsAdmin((PrivilegedStartupAction)() -> {
      if (shouldCreateAdminAccount() && !adminUserCreatedWithGivenPassword()) {
        createStartupToken();
      }
    });
  }

  @SuppressWarnings({"java:S2639", "java:S2629"}) // Yes, we use '.' as a regex here
                                                  // No, we do not need conditional execution for 'replaceAll' here
  private boolean adminUserCreatedWithGivenPassword() {
    String startupTokenByProperty = System.getProperty(INITIAL_PASSWORD_PROPERTY);
    if (startupTokenByProperty != null) {
      String adminUserName = System.getProperty(INITIAL_USER_PROPERTY, "scmadmin");
      context.runAsAdmin((PrivilegedStartupAction) () ->
        createAdminUser(adminUserName, "SCM Administrator", "scm-admin@scm-manager.org", startupTokenByProperty));
      LOG.info("================={}========================", adminUserName.replaceAll(".", "="));
      LOG.info("==               {}                      ==", adminUserName.replaceAll(".", " "));
      LOG.info("== Created user '{}' with given password ==", adminUserName);
      LOG.info("==               {}                      ==", adminUserName.replaceAll(".", " "));
      LOG.info("================={}========================", adminUserName.replaceAll(".", "="));
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String name() {
    return "adminAccount";
  }

  @Override
  public int sequence() {
    return 0;
  }

  @Override
  public boolean done() {
    return initialToken == null;
  }

  public void createAdminUser(String userName, String displayName, String email, String password) {
    User admin = new User(userName, displayName, email);
    String encryptedPassword = passwordService.encryptPassword(password);
    admin.setPassword(encryptedPassword);
    doThrow().violation("invalid user name").when(!admin.isValid());
    PermissionDescriptor descriptor = new PermissionDescriptor("*");
    context.runAsAdmin((PrivilegedStartupAction) () -> {
      userManager.create(admin);
      permissionAssigner.setPermissionsForUser(userName, Collections.singleton(descriptor));
      initialToken = null;
    });
  }

  @SuppressWarnings("java:S1192") // With duplication the log message is far better readable in the code
  private void createStartupToken() {
    initialToken = randomPasswordGenerator.createRandomPassword();
    LOG.warn("====================================================");
    LOG.warn("==                                                ==");
    LOG.warn("==    Startup token for initial user creation     ==");
    LOG.warn("==                                                ==");
    LOG.warn("==              {}              ==", initialToken);
    LOG.warn("==                                                ==");
    LOG.warn("====================================================");
  }

  private boolean shouldCreateAdminAccount() {
    return !Boolean.getBoolean("sonia.scm.skipAdminCreation") && (userManager.getAll().isEmpty() || onlyAnonymousUserExists());
  }

  private boolean onlyAnonymousUserExists() {
    return userManager.getAll().size() == 1 && userManager.contains(SCMContext.USER_ANONYMOUS);
  }

  public boolean isCorrectToken(String givenStartupToken) {
    return initialToken.equals(givenStartupToken);
  }
}
