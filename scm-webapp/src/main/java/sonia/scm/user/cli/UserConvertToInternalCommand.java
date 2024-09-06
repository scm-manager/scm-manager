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
import org.apache.shiro.authc.credential.PasswordService;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.util.ValidationUtil;

@ParentCommand(value = UserCommand.class)
@CommandLine.Command(name = "convert-to-internal", aliases = "conv-int")
class UserConvertToInternalCommand implements Runnable {

  @CommandLine.Mixin
  private final UserTemplateRenderer templateRenderer;
  private final UserManager manager;
  private final PasswordService passwordService;

  @CommandLine.Parameters(index = "0", paramLabel = "<username>", descriptionKey = "scm.user.username")
  private String username;

  @CommandLine.Parameters(index = "1", paramLabel = "<password>", descriptionKey = "scm.user.password")
  private String password;

  @Inject
  UserConvertToInternalCommand(UserTemplateRenderer templateRenderer, UserManager manager, PasswordService passwordService) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
    this.passwordService = passwordService;
  }

  @Override
  public void run() {
    User user = manager.get(username);

    if (user != null) {
      if (!ValidationUtil.isPasswordValid(password)) {
        templateRenderer.renderPasswordError();
      }
      user.setExternal(false);
      user.setPassword(passwordService.encryptPassword(password));
      manager.modify(user);
      templateRenderer.render(user);
    } else {
      templateRenderer.renderNotFoundError();
    }
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setPassword(String password) {
    this.password = password;
  }
}
