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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.RepositoryManager;

import java.util.Collection;
import java.util.stream.Collectors;

@ParentCommand(value = RepositoryCommand.class)
@CommandLine.Command(name = "list", aliases = "ls")
class RepositoryListCommand implements Runnable {

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final RepositoryManager manager;
  private final RepositoryToRepositoryCommandDtoMapper mapper;

  @CommandLine.Option(names = {"--short", "-s"})
  private boolean useShortTemplate;

  private static final String TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{#row.first}}{{#upper}}{{value}}{{/upper}}{{/row.first}}{{^row.first}}{{value}}{{/row.first}}{{^last}} {{/last}}{{/cols}}",
    "{{/rows}}"
  );

  private static final String SHORT_TEMPLATE = String.join("\n",
    "{{#repos}}",
    "{{namespace}}/{{name}}",
    "{{/repos}}"
  );

  @Inject
  public RepositoryListCommand(RepositoryManager manager, TemplateRenderer templateRenderer, RepositoryToRepositoryCommandDtoMapper mapper) {
    this.manager = manager;
    this.templateRenderer = templateRenderer;
    this.mapper = mapper;
  }

  @Override
  public void run() {
    Collection<RepositoryCommandBean> beans = manager.getAll().stream().map(mapper::map).collect(Collectors.toList());
    if (useShortTemplate) {
      templateRenderer.renderToStdout(SHORT_TEMPLATE, ImmutableMap.of("repos", beans));
    } else {
      Table table = templateRenderer.createTable();
      table.addHeader("repoName", "repoType", "repoUrl");
      for (RepositoryCommandBean bean : beans) {
        table.addRow(bean.getNamespace() + "/" + bean.getName(), bean.getType(), bean.getUrl());
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, ImmutableMap.of("rows", table, "repos", beans));
    }
  }

  @VisibleForTesting
  void setShortTemplate(boolean useShortTemplate) {
    this.useShortTemplate = useShortTemplate;
  }
}
