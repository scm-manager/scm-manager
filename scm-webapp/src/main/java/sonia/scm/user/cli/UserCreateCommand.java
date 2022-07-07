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

package sonia.scm.user.cli;

import com.google.common.annotations.VisibleForTesting;
import org.apache.shiro.authc.credential.PasswordService;
import picocli.CommandLine;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import javax.inject.Inject;
import javax.validation.constraints.Email;

@CommandLine.Command(name = "create")
@ParentCommand(value = UserCommand.class)
class UserCreateCommand implements Runnable {

  @CommandLine.Mixin
  private final UserTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final UserManager manager;
  private final PasswordService passwordService;

  @CommandLine.Parameters(index = "0", paramLabel = "<username>", descriptionKey = "scm.user.username")
  private String username;

  @CommandLine.Parameters(index = "1", paramLabel = "<displayname>", descriptionKey = "scm.user.displayName")
  private String displayName;

  @Email
  @CommandLine.Option(names = {"--email", "-e"}, descriptionKey = "scm.user.email")
  private String email;

  @CommandLine.Option(names = {"--external", "-x"}, descriptionKey = "scm.user.create.external")
  private boolean external;

  @CommandLine.Option(names = {"--password", "-p"}, descriptionKey = "scm.user.password")
  private String password;

  @CommandLine.Option(names = {"--deactivate", "-d"}, descriptionKey = "scm.user.create.deactivate")
  private boolean inactive;

  @Inject
  public UserCreateCommand(UserTemplateRenderer templateRenderer,
                           CommandValidator validator,
                           UserManager manager, PasswordService passwordService) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.manager = manager;
    this.passwordService = passwordService;
  }

  @Override
  public void run() {
    validator.validate();
    User newUser = new User();
    newUser.setName(username);
    newUser.setDisplayName(displayName);
    newUser.setMail(email);
    newUser.setExternal(external);
    if (!external) {
      if (password == null) {
        templateRenderer.renderPasswordError();
      }
      newUser.setPassword(passwordService.encryptPassword(password));
      newUser.setActive(!inactive);
    } else {
      if (inactive) {
        templateRenderer.renderExternalDeactivateError();
      }
    }
    User createdUser = manager.create(newUser);
    templateRenderer.render(createdUser);
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setUsername(String username) {
    this.username = username;
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setEmail(String email) {
    this.email = email;
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setExternal(boolean external) {
    this.external = external;
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setPassword(String password) {
    this.password = password;
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setInactive(boolean inactive) {
    this.inactive = inactive;
  }
}
