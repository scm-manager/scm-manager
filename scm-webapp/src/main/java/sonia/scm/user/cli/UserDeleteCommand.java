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
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import java.util.Collections;

@CommandLine.Command(name = "delete", aliases = "rm")
@ParentCommand(UserCommand.class)
class UserDeleteCommand implements Runnable {

  private static final String CONFIRM_TEMPLATE = "{{i18n.scmUserDeleteConfirm}}";
  private static final String SUCCESS_TEMPLATE = "{{i18n.scmUserDeleteSuccess}}";

  @CommandLine.Parameters(index = "0", paramLabel = "<username>", descriptionKey = "scm.user.username")
  private String username;

  @CommandLine.Option(names = {"--yes", "-y"}, descriptionKey = "scm.user.delete.prompt")
  private boolean shouldDelete;

  @CommandLine.Mixin
  private final UserTemplateRenderer templateRenderer;
  private final UserManager manager;

  @Inject
  public UserDeleteCommand(UserTemplateRenderer templateRenderer, UserManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    if (!shouldDelete) {
      templateRenderer.renderToStderr(CONFIRM_TEMPLATE, Collections.emptyMap());
      return;
    }
    User user = manager.get(username);
    if (user != null) {
      manager.delete(user);
      templateRenderer.renderToStdout(SUCCESS_TEMPLATE, Collections.emptyMap());
    }
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setShouldDelete(boolean shouldDelete) {
    this.shouldDelete = shouldDelete;
  }
}
