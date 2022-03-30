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

import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

@ParentCommand(value = RepositoryCommand.class)
@CommandLine.Command(name = "list", aliases = "ls")
public class RepositoryListCommand implements Runnable {

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final RepositoryManager manager;
  private final RepositoryToRepositoryCommandDtoMapper mapper;

  @CommandLine.Option(names = {"--short", "-s"})
  private boolean useShortTemplate;

  private static final String TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{value}}{{^last}} {{/last}}{{/cols}}",
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
    Collection<RepositoryCommandDto> dtos = manager.getAll().stream().map(mapper::map).collect(Collectors.toList());
    if (useShortTemplate) {
      templateRenderer.renderToStdout(SHORT_TEMPLATE, ImmutableMap.of("repos", dtos));
    } else {
      Table table = templateRenderer.createTable();
      table.addHeaderKeys("repoName", "repoType", "repoUrl");
      for (RepositoryCommandDto dto : dtos) {
        table.addRow(dto.getNamespace() + "/" + dto.getName(), dto.getType(), dto.getUrl());
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, ImmutableMap.of("rows", table, "repos", dtos));
    }
  }
}
