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
import sonia.scm.cli.CliContext;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.Repository;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import java.util.Collections;

public class RepositoryTemplateRenderer extends TemplateRenderer {

  private static final String DETAILS_TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{value}}{{^last}}: {{/last}}{{/cols}}",
    "{{/rows}}"
  );
  private static final String INVALID_INPUT_TEMPLATE = "{{i18n.repoInvalidInput}}";
  private static final String NOT_FOUND_TEMPLATE = "{{i18n.repoNotFound}}";

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
    RepositoryCommandDto dto = mapper.map(repository);
    table.addLabelValueRow("repoNamespace", dto.getNamespace());
    table.addLabelValueRow("repoName", dto.getName());
    table.addLabelValueRow("repoType", dto.getType());
    table.addLabelValueRow("repoContact", dto.getContact());
    table.addLabelValueRow("repoUrl", dto.getUrl());
    table.addLabelValueRow("repoDescription", dto.getDescription());
    renderToStdout(DETAILS_TABLE_TEMPLATE, ImmutableMap.of("rows", table, "repo", dto));
  }

  public void renderInvalidInputError() {
    renderToStderr(INVALID_INPUT_TEMPLATE, Collections.emptyMap());
    context.exit(2);
  }

  public void renderNotFoundError() {
    renderToStderr(NOT_FOUND_TEMPLATE, Collections.emptyMap());
    context.exit(1);
  }

  public void renderException(Exception exception) {
    renderDefaultError(exception);
    context.exit(1);
  }
}
