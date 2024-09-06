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
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ParentCommand(value = UserCommand.class)
@CommandLine.Command(name = "remove-permissions")
class UserPermissionRemoveCommand implements Runnable {

  @CommandLine.Parameters(index = "0", paramLabel = "<username>", descriptionKey = "scm.user.name")
  private String name;

  @CommandLine.Parameters(index = "1..", arity = "1..", paramLabel = "<permission>", descriptionKey = "scm.user.permissions")
  private String[] removedPermissions;

  @CommandLine.Mixin
  private final UserTemplateRenderer templateRenderer;
  private final PermissionAssigner permissionAssigner;
  private final UserManager userManager;
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;

  @Inject
  UserPermissionRemoveCommand(UserTemplateRenderer templateRenderer, PermissionAssigner permissionAssigner, UserManager userManager) {
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
    Collection<PermissionDescriptor> permissions = permissionAssigner.readPermissionsForUser(name);
    permissionAssigner.setPermissionsForUser(name, getReducedPermissions(permissions));
  }

  private List<PermissionDescriptor> getReducedPermissions(Collection<PermissionDescriptor> permissions) {
    return permissions.stream()
      .filter(p -> Arrays.stream(removedPermissions)
        .noneMatch(rp -> rp.equals(p.getValue()))
      )
      .collect(Collectors.toList());
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setRemovedPermissions(String[] removedPermissions) {
    this.removedPermissions = removedPermissions;
  }
}
