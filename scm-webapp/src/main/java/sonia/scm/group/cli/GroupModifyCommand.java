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

import static java.util.Arrays.asList;

@ParentCommand(GroupCommand.class)
@CommandLine.Command(name = "modify")
class GroupModifyCommand implements Runnable {

  @CommandLine.Mixin
  private final GroupTemplateRenderer templateRenderer;
  private final GroupManager manager;

  @CommandLine.Parameters(descriptionKey = "scm.group.modify.name")
  private String name;

  @CommandLine.Option(names = {"--description", "-d"}, descriptionKey = "scm.group.modify.desc")
  private String description;

  @CommandLine.Option(names = {"--member", "-m"}, descriptionKey = "scm.group.modify.member")
  private String[] members;

  @CommandLine.Option(names = {"--external", "-e"}, descriptionKey = "scm.group.modify.external")
  private Boolean external;

  @Inject
  GroupModifyCommand(GroupTemplateRenderer templateRenderer, GroupManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    Group existingGroup = manager.get(name);
    if (existingGroup == null) {
      templateRenderer.renderNotFoundError();
    } else {
      if (description != null) {
        existingGroup.setDescription(description);
      }
      if (external != null) {
        existingGroup.setExternal(external);
      }
      if (members != null) {
        existingGroup.setMembers(asList(members));
      }
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

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setDescription(String description) {
    this.description = description;
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setMembers(String[] members) {
    this.members = members;
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setExternal(boolean external) {
    this.external = external;
  }
}
