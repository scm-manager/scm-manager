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

package sonia.scm.group.cli;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

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
