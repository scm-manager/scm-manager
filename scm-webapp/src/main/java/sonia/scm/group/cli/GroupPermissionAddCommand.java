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
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;

import java.util.Collection;

@ParentCommand(value = GroupCommand.class)
@CommandLine.Command(name = "add-permissions")
class GroupPermissionAddCommand implements Runnable {

  @CommandLine.Parameters(index = "0", paramLabel = "<name>", descriptionKey = "scm.group.name")
  private String name;

  @CommandLine.Parameters(index = "1..", arity = "1..", paramLabel = "<permission>", descriptionKey = "scm.group.permissions")
  private String[] addedPermissions;

  @CommandLine.Mixin
  private final GroupTemplateRenderer templateRenderer;
  private final PermissionAssigner permissionAssigner;
  private final GroupManager groupManager;
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;

  @Inject
  GroupPermissionAddCommand(GroupTemplateRenderer templateRenderer, PermissionAssigner permissionAssigner, GroupManager groupManager) {
    this.templateRenderer = templateRenderer;
    this.permissionAssigner = permissionAssigner;
    this.groupManager = groupManager;
  }

  @Override
  public void run() {
    Group group = groupManager.get(name);
    if (group == null) {
      templateRenderer.renderNotFoundError();
      return;
    }
    Collection<PermissionDescriptor> permissions = permissionAssigner.readPermissionsForGroup(name);
    for (String addedPermission : addedPermissions) {
      if (isPermissionInvalid(addedPermission)) {
        templateRenderer.renderUnknownPermissionError(addedPermission);
        return;
      }
      permissions.add(new PermissionDescriptor(addedPermission));
    }
    permissionAssigner.setPermissionsForGroup(name, permissions);
  }

  private boolean isPermissionInvalid(String permission) {
    return permissionAssigner.getAvailablePermissions()
      .stream()
      .noneMatch(p -> p.getValue().equals(permission));
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }
  @VisibleForTesting
  void setAddedPermissions(String[] addedPermissions) {
    this.addedPermissions = addedPermissions;
  }
}
