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
import com.google.common.collect.ImmutableMap;
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.group.GroupManager;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

@ParentCommand(GroupCommand.class)
@CommandLine.Command(name = "list", aliases = "ls")
class GroupListCommand implements Runnable {

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final GroupManager manager;
  private final GroupCommandBeanMapper beanMapper;
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
    "{{#groups}}",
    "{{name}}",
    "{{/groups}}"
  );

  @Inject
  GroupListCommand(TemplateRenderer templateRenderer, GroupManager manager, GroupCommandBeanMapper beanMapper) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
    this.beanMapper = beanMapper;
  }

  @Override
  public void run() {
    Collection<GroupCommandBean> groupCommandBeans = manager.getAll().stream().map(beanMapper::map).collect(toList());
    if (useShortTemplate) {
      templateRenderer.renderToStdout(SHORT_TEMPLATE, ImmutableMap.of("groups", groupCommandBeans));
    } else {
      Table table = templateRenderer.createTable();
      table.addHeader("scm.group.name", "scm.group.external");
      String yes = spec.resourceBundle().getString("yes");
      String no = spec.resourceBundle().getString("no");
      for (GroupCommandBean bean : groupCommandBeans) {
        table.addRow(bean.getName(), bean.isExternal()? yes: no);
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, ImmutableMap.of("rows", table, "groups", groupCommandBeans));
    }
  }

  @VisibleForTesting
  void setUseShortTemplate(boolean useShortTemplate) {
    this.useShortTemplate = useShortTemplate;
  }

  @VisibleForTesting
  void setSpec(CommandLine.Model.CommandSpec spec) {
    this.spec = spec;
  }
}
