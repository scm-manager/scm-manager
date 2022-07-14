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

package sonia.scm.repository.cli;

import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.cli.Table;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@CommandLine.Command(name = "list-permissions")
@ParentCommand(value = RepositoryCommand.class)
public class RepositoryListPermissionsCommand implements Runnable {

  private static final String TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{#row.first}}{{#upper}}{{value}}{{/upper}}{{/row.first}}{{^row.first}}{{value}}{{/row.first}}{{^last}} {{/last}}{{/cols}}",
    "{{/rows}}"
  );

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final RepositoryManager manager;
  private final RepositoryRoleManager roleManager;
  private final PermissionDescriptionResolver permissionDescriptionResolver;

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0", descriptionKey = "scm.repo.list-permissions.repository")
  private String repository;

  @Inject
  public RepositoryListPermissionsCommand(RepositoryTemplateRenderer templateRenderer, CommandValidator validator, RepositoryManager manager, RepositoryRoleManager roleManager, PermissionDescriptionResolver permissionDescriptionResolver) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.manager = manager;
    this.roleManager = roleManager;
    this.permissionDescriptionResolver = permissionDescriptionResolver;
  }

  @VisibleForTesting
  void setRepository(String repository) {
    this.repository = repository;
  }

  @Override
  public void run() {
    validator.validate();
    String[] splitRepo = repository.split("/");
    if (splitRepo.length == 2) {
      Repository repo = manager.get(new NamespaceAndName(splitRepo[0], splitRepo[1]));

      if (repo != null) {
        Collection<RepositoryPermission> permissions = repo.getPermissions();
        Table table = templateRenderer.createTable();
        table.addHeader("isGroup", "name", "role", "verbs");
        for (RepositoryPermission permission : permissions) {
          addPermissionToTable(table, permission);
        }
        templateRenderer.renderToStdout(TABLE_TEMPLATE, Map.ofEntries(entry("rows", table), entry("permissions", permissions)));
      } else {
        templateRenderer.renderNotFoundError();
      }
    } else {
      templateRenderer.renderInvalidInputError();
    }
  }

  private void addPermissionToTable(Table table, RepositoryPermission permission) {
    Collection<String> effectiveVerbs;
    if (permission.getRole() == null) {
      effectiveVerbs = permission.getVerbs();
    } else {
      effectiveVerbs = roleManager.get(permission.getRole()).getVerbs();
    }
    Collection<String> verbDescriptions = getDescriptions(effectiveVerbs);
    table.addRow(permission.isGroupPermission()? "X": "", permission.getName(), permission.getRole(), String.join(", ", verbDescriptions));
  }

  private Collection<String> getDescriptions(Collection<String> effectiveVerbs) {
    return effectiveVerbs.stream().map(this::getDescription).collect(Collectors.toList());
  }

  private String getDescription(String verb) {
    return permissionDescriptionResolver.getDescription(verb).orElse(verb);
  }
}
