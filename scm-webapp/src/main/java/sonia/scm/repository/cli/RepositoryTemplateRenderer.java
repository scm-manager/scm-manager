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

import sonia.scm.cli.CliContext;
import sonia.scm.cli.ExitCode;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.Repository;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Map.entry;

class RepositoryTemplateRenderer extends TemplateRenderer {

  private static final String DETAILS_TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{value}}{{/cols}}",
    "{{/rows}}"
  );
  private static final String TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{#row.first}}{{#upper}}{{value}}{{/upper}}{{/row.first}}{{^row.first}}{{value}}{{/row.first}}{{^last}} {{/last}}{{/cols}}",
    "{{/rows}}"
  );
  private static final String INVALID_INPUT_TEMPLATE = "{{i18n.repoInvalidInput}}";
  private static final String NOT_FOUND_TEMPLATE = "{{i18n.repoNotFound}}";
  private static final String ROLE_NOT_FOUND_TEMPLATE = "{{i18n.roleNotFound}}";

  private final CliContext context;
  private final RepositoryToRepositoryCommandDtoMapper mapper;

  @Inject
  RepositoryTemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory, RepositoryToRepositoryCommandDtoMapper mapper) {
    super(context, templateEngineFactory);
    this.context = context;
    this.mapper = mapper;
  }

  public void render(Repository repository) {
    Table table = createTable();
    RepositoryCommandBean bean = mapper.map(repository);
    table.addLabelValueRow("repoNamespace", bean.getNamespace());
    table.addLabelValueRow("repoName", bean.getName());
    table.addLabelValueRow("repoType", bean.getType());
    table.addLabelValueRow("repoContact", bean.getContact());
    table.addLabelValueRow("repoCreationDate", bean.getCreationDate());
    table.addLabelValueRow("repoLastModified", bean.getLastModified());
    table.addLabelValueRow("repoUrl", bean.getUrl());
    table.addLabelValueRow("repoDescription", bean.getDescription());
    renderToStdout(DETAILS_TABLE_TEMPLATE, Map.of("rows", table, "repo", bean));
  }

  public void render(Collection<RepositoryPermissionBean> permissions) {
    Table table = createTable();
    table.addHeader("scm.repo.permissions.type", "scm.repo.permissions.name", "scm.repo.permissions.role");
    permissions.forEach(permission -> addPermissionToTable(table, permission));
    renderToStdout(TABLE_TEMPLATE, Map.ofEntries(entry("rows", table), entry("permissions", permissions)));
  }

  public void renderVerbose(Collection<RepositoryPermissionBean> permissions) {
    Table table = createTable();
    table.addHeader("scm.repo.permissions.type", "scm.repo.permissions.name", "scm.repo.permissions.role", "scm.repo.permissions.verbs");
    permissions.forEach(permission -> addVerbosePermissionToTable(table, permission));
    renderToStdout(TABLE_TEMPLATE, Map.ofEntries(entry("rows", table), entry("permissions", permissions)));
  }

  private void addPermissionToTable(Table table, RepositoryPermissionBean permission) {
    table.addRow(
      getBundle().getString(permission.isGroupPermission()? "scm.repo.permissions.isGroup": "scm.repo.permissions.isUser"),
      permission.getName(),
      permission.getRole()
    );
  }

  private void addVerbosePermissionToTable(Table table, RepositoryPermissionBean permission) {
    table.addRow(
      getBundle().getString(permission.isGroupPermission()? "scm.repo.permissions.isGroup": "scm.repo.permissions.isUser"),
      permission.getName(),
      permission.getRole(),
      String.join(", ", permission.getVerbs())
    );
  }

  public void renderInvalidInputError() {
    renderToStderr(INVALID_INPUT_TEMPLATE, emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }

  public void renderNotFoundError() {
    renderToStderr(NOT_FOUND_TEMPLATE, emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.NOT_FOUND);
  }

  public void renderRoleNotFoundError() {
    renderToStderr(ROLE_NOT_FOUND_TEMPLATE, emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.NOT_FOUND);
  }

  public void renderException(Exception exception) {
    renderDefaultError(exception);
    context.exit(ExitCode.SERVER_ERROR);
  }

  public void renderVerbs(Collection<VerbBean> verbs) {
    Table table = createTable();
    table.addHeader("scm.repo.permissions.verb", "scm.repo.permissions.description");
    verbs.forEach(verb -> addVerbToTable(table, verb));
    renderToStdout(TABLE_TEMPLATE, Map.ofEntries(entry("rows", table), entry("verbs", verbs)));
  }

  private void addVerbToTable(Table table, VerbBean verb) {
    table.addRow(verb.getVerb(), verb.getDescription());
  }

  public void renderRoles(Collection<RoleBean> roles) {
    Table table = createTable();
    table.addHeader("scm.repo.permissions.role", "scm.repo.permissions.verbs");
    roles.forEach(role -> addRoleToTable(table, role));
    renderToStdout(TABLE_TEMPLATE, Map.ofEntries(entry("rows", table), entry("roles", roles)));
  }

  private void addRoleToTable(Table table, RoleBean role) {
    table.addRow(role.getName(), String.join(", ", role.getVerbs()));
  }

  public void render(Collection<RoleBean> roles, Collection<VerbBean> verbs) {
    renderRoles(roles);
    renderToStdout("\n", emptyMap());
    renderVerbs(verbs);
  }
}
