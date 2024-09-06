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
import com.google.common.collect.ImmutableMap;
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.user.UserManager;

import java.util.Collection;
import java.util.stream.Collectors;

@ParentCommand(value = UserCommand.class)
@CommandLine.Command(name = "list", aliases = "ls")
class UserListCommand implements Runnable {

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final UserManager manager;
  private final UserCommandBeanMapper mapper;
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;

  @CommandLine.Option(names = {"--short", "-s"})
  private boolean useShortTemplate;

  private static final String TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{#row.first}}{{#upper}}{{value}}{{/upper}}{{/row.first}}{{^row.first}}{{value}}{{/row.first}}{{^last}} {{/last}}{{/cols}}",
    "{{/rows}}"
  );

  private static final String SHORT_TEMPLATE = String.join("\n",
    "{{#users}}",
    "{{name}}",
    "{{/users}}"
  );

  @Inject
  public UserListCommand(TemplateRenderer templateRenderer, UserManager manager, UserCommandBeanMapper mapper) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
    this.mapper = mapper;
  }

  @Override
  public void run() {
    Collection<UserCommandBean> beans = manager.getAll().stream().map(mapper::map).collect(Collectors.toList());
    if (useShortTemplate) {
      templateRenderer.renderToStdout(SHORT_TEMPLATE, ImmutableMap.of("users", beans));
    } else {
      Table table = templateRenderer.createTable();
      String yes = spec.resourceBundle().getString("yes");
      String no = spec.resourceBundle().getString("no");
      table.addHeader("scm.user.username", "scm.user.displayName", "scm.user.email", "scm.user.external", "scm.user.active");
      for (UserCommandBean bean : beans) {
        table.addRow(bean.getName(), bean.getDisplayName(), bean.getMail(), bean.isExternal() ? yes : no, bean.isActive() ? yes : no);
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, ImmutableMap.of("rows", table, "users", beans));
    }
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  void setUseShortTemplate(boolean useShortTemplate) {
    this.useShortTemplate = useShortTemplate;
  }

  @VisibleForTesting
  void setSpec(CommandLine.Model.CommandSpec spec) {
    this.spec = spec;
  }
}
