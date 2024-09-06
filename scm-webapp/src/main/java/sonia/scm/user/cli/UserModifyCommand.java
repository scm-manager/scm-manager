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

package sonia.scm.user.cli;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Email;
import org.apache.shiro.authc.credential.PasswordService;
import picocli.CommandLine;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.util.ValidationUtil;

@ParentCommand(value = UserCommand.class)
@CommandLine.Command(name = "modify")
class UserModifyCommand implements Runnable {

  @CommandLine.Mixin
  private final UserTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final UserManager manager;
  private final PasswordService passwordService;

  @CommandLine.Parameters(index = "0", paramLabel = "<username>", descriptionKey = "scm.user.username")
  private String username;

  @CommandLine.Option(names = {"--displayname", "-d"}, descriptionKey = "scm.user.displayName")
  private String displayName;

  @Email
  @CommandLine.Option(names = {"--email", "-e"}, descriptionKey = "scm.user.email")
  private String email;

  @CommandLine.Option(names = {"--password", "-p"}, descriptionKey = "scm.user.password")
  private String password;

  @Inject
  UserModifyCommand(UserTemplateRenderer templateRenderer, CommandValidator validator, UserManager manager, PasswordService passwordService) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.manager = manager;
    this.passwordService = passwordService;
  }

  @Override
  public void run() {
    validator.validate();
    if (password != null && !ValidationUtil.isPasswordValid(password)) {
      templateRenderer.renderPasswordError();
    }

    User user = manager.get(username);

    if (user != null) {
      if (displayName != null) {
        user.setDisplayName(displayName);
      }
      if (email != null) {
        user.setMail(email);
      }
      if (!user.isExternal() && password != null) {
        user.setPassword(passwordService.encryptPassword(password));
      }
      manager.modify(user);
      templateRenderer.render(user);
    } else {
      templateRenderer.renderNotFoundError();
    }
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
  void setPassword(String password) {
    this.password = password;
  }
}
