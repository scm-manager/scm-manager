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

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.shiro.authc.credential.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.config.ConfigValue;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;

import java.util.Collections;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Extension
@Singleton
public class AdminAccountStartupAction implements InitializationStep {

  private static final Logger LOG = LoggerFactory.getLogger(AdminAccountStartupAction.class);

  private final PasswordService passwordService;
  private final UserManager userManager;
  private final PermissionAssigner permissionAssigner;
  private final RandomPasswordGenerator randomPasswordGenerator;
  private final AdministrationContext context;
  private final String adminUserName;
  private final String initialPassword;
  private final Boolean skipAdminCreation;

  private String initialToken;

  @Inject
  public AdminAccountStartupAction(
    @ConfigValue(key = "initialUser", defaultValue = "scmadmin", description = "Initial user for admin account") String initialUser,
    @ConfigValue(key = "initialPassword", defaultValue = "", description = "Initial password for admin account") String initialPassword,
    @ConfigValue(key = "skipAdminCreation", defaultValue = "false", description = "Skip creation of initial admin user") Boolean skipAdminCreation,
    PasswordService passwordService,
    UserManager userManager,
    PermissionAssigner permissionAssigner,
    RandomPasswordGenerator randomPasswordGenerator,
    AdministrationContext context
  ) {
    this.initialPassword = initialPassword;
    this.adminUserName = initialUser;
    this.skipAdminCreation = skipAdminCreation;
    this.passwordService = passwordService;
    this.userManager = userManager;
    this.permissionAssigner = permissionAssigner;
    this.randomPasswordGenerator = randomPasswordGenerator;
    this.context = context;

    initialize();
  }

  private void initialize() {
    context.runAsAdmin((PrivilegedStartupAction) () -> {
      if (shouldCreateAdminAccount() && !adminUserCreatedWithGivenPassword()) {
        createStartupToken();
      }
    });
  }

  @SuppressWarnings({"java:S2639", "java:S2629"}) // Yes, we use '.' as a regex here
  // No, we do not need conditional execution for 'replaceAll' here
  private boolean adminUserCreatedWithGivenPassword() {
    if (!Strings.isNullOrEmpty(initialPassword)) {
      context.runAsAdmin((PrivilegedStartupAction) () ->
        createAdminUser(adminUserName, "SCM Administrator", "scm@example.com", initialPassword));
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
    return !skipAdminCreation && (userManager.getAll().isEmpty() || onlyAnonymousUserExists());
  }

  private boolean onlyAnonymousUserExists() {
    return userManager.getAll().size() == 1 && userManager.contains(SCMContext.USER_ANONYMOUS);
  }

  public boolean isCorrectToken(String givenStartupToken) {
    return initialToken.equals(givenStartupToken);
  }
}
