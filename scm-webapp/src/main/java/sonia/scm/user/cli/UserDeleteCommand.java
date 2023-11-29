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
