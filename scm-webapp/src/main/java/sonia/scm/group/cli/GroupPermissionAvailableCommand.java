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

import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.cli.Table;
import sonia.scm.repository.cli.GroupCommand;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;

import javax.inject.Inject;
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
