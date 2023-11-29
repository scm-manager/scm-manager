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

