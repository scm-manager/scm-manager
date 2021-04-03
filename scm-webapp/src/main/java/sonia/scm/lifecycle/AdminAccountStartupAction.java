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

import com.fasterxml.jackson.databind.JsonNode;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.api.v2.resources.AdminAccountStartupResource;
import sonia.scm.api.v2.resources.InitializationResource;
import sonia.scm.api.v2.resources.InitializationStepResource;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;

import static de.otto.edison.hal.Link.link;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Extension
@Singleton
public class AdminAccountStartupAction implements InitializationStep {

  private static final Logger LOG = LoggerFactory.getLogger(AdminAccountStartupAction.class);

  private static final String INITIAL_PASSWORD_PROPERTY = "scm.initialPassword";

  private final PasswordService passwordService;
  private final UserManager userManager;
  private final PermissionAssigner permissionAssigner;
  private final RandomPasswordGenerator randomPasswordGenerator;
  private final AdministrationContext context;
  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  private String initialToken;

  @Inject
  public AdminAccountStartupAction(PasswordService passwordService, UserManager userManager, PermissionAssigner permissionAssigner, RandomPasswordGenerator randomPasswordGenerator, AdministrationContext context, Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.passwordService = passwordService;
    this.userManager = userManager;
    this.permissionAssigner = permissionAssigner;
    this.randomPasswordGenerator = randomPasswordGenerator;
    this.context = context;
    this.scmPathInfoStore = scmPathInfoStore;

    initialize(context);
  }

  private void initialize(AdministrationContext context) {
    context.runAsAdmin(() -> {
      if (shouldCreateAdminAccount() && !adminUserCreatedWithGivenPassword()) {
        createInitialPassword();
      }
    });
  }

  private boolean adminUserCreatedWithGivenPassword() {
    String initialPasswordByProperty = System.getProperty(INITIAL_PASSWORD_PROPERTY);
    if (initialPasswordByProperty != null) {
      createAdminUser("scmadmin", initialPasswordByProperty);
      LOG.info("=================================================");
      LOG.info("==                                             ==");
      LOG.info("== Created user 'scmadmin' with given password ==");
      LOG.info("==                                             ==");
      LOG.info("=================================================");
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

  @Override
  public void setupIndex(Links.Builder builder, Embedded.Builder embeddedBuilder) {
    String link =
      new LinkBuilder(scmPathInfoStore.get().get(), InitializationResource.class, AdminAccountStartupResource.class)
        .method("step").parameters(name())
        .method("post").parameters()
        .href();
    builder.single(link("initialAdminUser", link));
  }

  public void createAdminUser(String userName, String password) {
    User admin = new User(userName, "SCM Administrator", "scm-admin@scm-manager.org");
    String encryptedPassword = passwordService.encryptPassword(password);
    admin.setPassword(encryptedPassword);
    doThrow().violation("invalid user name").when(!admin.isValid());
    PermissionDescriptor descriptor = new PermissionDescriptor("*");
    context.runAsAdmin(() -> {
      userManager.create(admin);
      permissionAssigner.setPermissionsForUser(userName, Collections.singleton(descriptor));
      initialToken = null;
    });
  }

  private void createInitialPassword() {
    initialToken = randomPasswordGenerator.createRandomPassword();
    LOG.warn("====================================================");
    LOG.warn("==                                                ==");
    LOG.warn("==     Random token for initial user creation     ==");
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

  public boolean isCorrectToken(String givenInitialPassword) {
    return initialToken.equals(givenInitialPassword);
  }
}
