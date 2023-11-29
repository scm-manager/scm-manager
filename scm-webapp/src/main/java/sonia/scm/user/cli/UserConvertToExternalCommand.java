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

import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

@ParentCommand(value = UserCommand.class)
@CommandLine.Command(name = "convert-to-external", aliases = "conv-ext")
class UserConvertToExternalCommand implements Runnable {

  @CommandLine.Mixin
  private final UserTemplateRenderer templateRenderer;
  private final UserManager manager;

  @CommandLine.Parameters(index = "0", paramLabel = "<username>", descriptionKey = "scm.user.username")
  private String username;

  @Inject
  UserConvertToExternalCommand(UserTemplateRenderer templateRenderer, UserManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    User user = manager.get(username);

    if (user != null) {
      user.setExternal(true);
      manager.modify(user);
      templateRenderer.render(user);
    } else {
      templateRenderer.renderNotFoundError();
    }
  }
}
