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
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ParentCommand(value = GroupCommand.class)
@CommandLine.Command(name = "list-permissions")
class GroupPermissionListCommand implements Runnable {

  @CommandLine.Parameters(index = "0", paramLabel = "<name>", descriptionKey = "scm.group.name")
  private String name;

  @CommandLine.Option(names = {"--keys", "-k"}, descriptionKey = "scm.group.list-permissions.keys")
  private boolean keys;

  @CommandLine.Mixin
  private final GroupTemplateRenderer templateRenderer;
  private final PermissionAssigner permissionAssigner;
  private final PermissionDescriptionResolver descriptionResolver;
  private final GroupManager groupManager;
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;


  @Inject
  GroupPermissionListCommand(GroupTemplateRenderer templateRenderer, PermissionAssigner permissionAssigner, PermissionDescriptionResolver descriptionResolver, GroupManager groupManager) {
    this.templateRenderer = templateRenderer;
    this.permissionAssigner = permissionAssigner;
    this.descriptionResolver = descriptionResolver;
    this.groupManager = groupManager;
  }


  @Override
  public void run() {
    Collection<PermissionDescriptor> permissions;
    Group group = groupManager.get(name);
    if (group == null) {
      templateRenderer.renderNotFoundError();
      return;
    } else {
      permissions = permissionAssigner.readPermissionsForGroup(name);
    }
    if (keys) {
      templateRenderer.render(resolvePermissions(permissions));
    } else {
      templateRenderer.render(resolvePermissionDescriptions(permissions));
    }
  }

  private List<String> resolvePermissions(Collection<PermissionDescriptor> permissions) {
    return permissions.stream().map(PermissionDescriptor::getValue).collect(Collectors.toList());
  }

  private List<String> resolvePermissionDescriptions(Collection<PermissionDescriptor> permissions) {
    return permissions.stream()
      .map(p -> descriptionResolver.getGlobalDescription(p.getValue()).orElse(p.getValue()))
      .collect(Collectors.toList());
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setKeys(boolean keys) {
    this.keys = keys;
  }
}

