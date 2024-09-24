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

package sonia.scm.repository.cli;

import jakarta.inject.Inject;
import sonia.scm.cli.CliContext;
import sonia.scm.cli.ExitCode;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.Repository;
import sonia.scm.template.TemplateEngineFactory;

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

  private static final String TYPE_HEADER_KEY = "scm.repo.permissions.type";
  private static final String NAME_HEADER_KEY = "scm.repo.permissions.name";
  private static final String ROLE_HEADER_KEY = "scm.repo.permissions.role";
  private static final String VERBS_HEADER_KEY = "scm.repo.permissions.verbs";

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
    table.addHeader(TYPE_HEADER_KEY, NAME_HEADER_KEY, ROLE_HEADER_KEY);
    permissions.forEach(permission -> addPermissionToTable(table, permission));
    renderToStdout(TABLE_TEMPLATE, Map.ofEntries(entry("rows", table), entry("permissions", permissions)));
  }

  public void renderVerbose(Collection<RepositoryPermissionBean> permissions) {
    Table table = createTable();
    table.addHeader(TYPE_HEADER_KEY, NAME_HEADER_KEY, ROLE_HEADER_KEY, VERBS_HEADER_KEY);
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

  void renderRoleNotFoundError() {
    renderToStderr("{{i18n.roleNotFound}}", emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.NOT_FOUND);
  }

  void renderVerbNotFoundError(String verb) {
    renderToStderr("{{i18n.verbNotFound}}: {{verb}}", Map.of("verb", verb));
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
    table.addHeader(ROLE_HEADER_KEY, VERBS_HEADER_KEY);
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
