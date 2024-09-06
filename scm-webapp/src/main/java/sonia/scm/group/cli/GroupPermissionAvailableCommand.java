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

import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.cli.Table;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;

import java.util.Collection;
import java.util.Map;

@ParentCommand(value = GroupCommand.class)
@CommandLine.Command(name = "available-permissions")
class GroupPermissionAvailableCommand implements Runnable {

  private static final String TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{#row.first}}{{#upper}}{{value}}{{/upper}}{{/row.first}}{{^row.first}}{{value}}{{/row.first}}{{^last}} {{/last}}{{/cols}}",
    "{{/rows}}"
  );

  @CommandLine.Mixin
  private final GroupTemplateRenderer templateRenderer;
  private final PermissionAssigner permissionAssigner;
  private final PermissionDescriptionResolver descriptionResolver;
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;

  @Inject
  GroupPermissionAvailableCommand(GroupTemplateRenderer templateRenderer, PermissionAssigner permissionAssigner, PermissionDescriptionResolver descriptionResolver) {
    this.templateRenderer = templateRenderer;
    this.permissionAssigner = permissionAssigner;
    this.descriptionResolver = descriptionResolver;
  }

  @Override
  public void run() {
    Collection<PermissionDescriptor> availablePermissions = permissionAssigner.getAvailablePermissions();
    Table table = templateRenderer.createTable();
    table.addHeader("value", "description");
    for (PermissionDescriptor descriptor : availablePermissions) {
      String verb = descriptor.getValue();
      table.addRow(verb, descriptionResolver.getGlobalDescription(verb).orElse(verb));
    }
    templateRenderer.renderToStdout(TABLE_TEMPLATE, Map.of("rows", table, "permissions", availablePermissions));
  }
}
