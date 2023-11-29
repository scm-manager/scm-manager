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
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import java.util.Collections;

@ParentCommand(value = UserCommand.class)
@CommandLine.Command(name = "clear-permissions")
class UserPermissionClearCommand implements Runnable {

  @CommandLine.Parameters(index = "0", paramLabel = "<username>", descriptionKey = "scm.user.name")
  private String name;

  @CommandLine.Mixin
  private final UserTemplateRenderer templateRenderer;
  private final PermissionAssigner permissionAssigner;
  private final UserManager userManager;
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;

  @Inject
  UserPermissionClearCommand(UserTemplateRenderer templateRenderer, PermissionAssigner permissionAssigner, UserManager userManager) {
    this.templateRenderer = templateRenderer;
    this.permissionAssigner = permissionAssigner;
    this.userManager = userManager;
  }

  @Override
  public void run() {
    User user = userManager.get(name);
    if (user == null) {
      templateRenderer.renderNotFoundError();
      return;
    }
    permissionAssigner.setPermissionsForUser(name, Collections.emptyList());
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }
}
