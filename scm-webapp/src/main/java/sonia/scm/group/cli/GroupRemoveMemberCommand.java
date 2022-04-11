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

package sonia.scm.group.cli;

import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.repository.cli.GroupCommand;

import javax.inject.Inject;
import java.util.Arrays;

@ParentCommand(GroupCommand.class)
@CommandLine.Command(name = "remove-member", aliases = "remove")
class GroupRemoveMemberCommand implements Runnable {

  @CommandLine.Mixin
  private final GroupTemplateRenderer templateRenderer;
  private final GroupManager manager;

  @CommandLine.Parameters(index = "0", arity = "1", descriptionKey = "scm.group.remove-member.name")
  private String name;
  @CommandLine.Parameters(index = "1..", arity = "1..", descriptionKey = "scm.group.remove-member.members")
  private String[] members;

  @Inject
  GroupRemoveMemberCommand(GroupTemplateRenderer templateRenderer, GroupManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    Group existingGroup = manager.get(name);
    if (existingGroup == null) {
      templateRenderer.renderNotFoundError();
    } else {
      Arrays.stream(members).forEach(existingGroup::remove);
      manager.modify(existingGroup);
      Group modifiedGroup = manager.get(name);
      templateRenderer.render(modifiedGroup);
    }
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setMembers(String[] members) {
    this.members = members;
  }
}
