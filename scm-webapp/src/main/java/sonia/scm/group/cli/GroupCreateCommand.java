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

@ParentCommand(GroupCommand.class)
@CommandLine.Command(name = "create")
class GroupCreateCommand implements Runnable {

  @CommandLine.Mixin
  private final GroupTemplateRenderer templateRenderer;
  private final GroupManager manager;

  @CommandLine.Parameters(descriptionKey = "scm.group.create.name")
  private String name;

  @CommandLine.Option(names = {"--description", "-d"}, descriptionKey = "scm.group.create.desc")
  private String description;

  @CommandLine.Option(names = {"--member", "-m"}, descriptionKey = "scm.group.create.member")
  private String[] members;

  @CommandLine.Option(names = {"--external", "-e"}, descriptionKey = "scm.group.create.external")
  private boolean external;

  @Inject
  GroupCreateCommand(GroupTemplateRenderer templateRenderer, GroupManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    Group newGroup = new Group("xml", name, members);
    newGroup.setDescription(description);
    newGroup.setExternal(external);
    Group createdGroup = manager.create(newGroup);
    templateRenderer.render(createdGroup);
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

  @VisibleForTesting
  void setMembers(String[] members) {
    this.members = members;
  }
}
