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

import com.cronutils.utils.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;
import sonia.scm.cli.CliContext;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;

@ParentCommand(value = RepositoryCommand.class)
@CommandLine.Command(name = "get")
public class RepositoryGetCommand implements Runnable {

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0")
  private String repository;

  static final String DEFAULT_TEMPLATE = String.join("\n",
    "{{repo.namespace}}/{{repo.name}}",
    "{{i18n.repoDescription}}: {{repo.description}}",
    "{{i18n.repoType}}: {{repo.type}}",
    "{{i18n.repoUrl}}: {{repo.url}}"
  );

  private static final String NOT_FOUND_TEMPLATE = "{{i18n.repoNotFound}}: {{repository}}";
  static final String INVALID_TEMPLATE = "{{i18n.repoInvalidInput}}: {{repository}}.";

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final CliContext context;
  private final RepositoryManager manager;
  private final RepositoryToRepositoryCommandDtoMapper mapper;

  @Inject
  RepositoryGetCommand(CliContext context, TemplateRenderer templateRenderer, RepositoryManager manager, RepositoryToRepositoryCommandDtoMapper mapper) {
    this.templateRenderer = templateRenderer;
    this.context = context;
    this.manager = manager;
    this.mapper = mapper;
  }

  @VisibleForTesting
  public void setRepository(String repository) {
    this.repository = repository;
  }

  @Override
  public void run() {
    String[] splitRepo = repository.split("/");
    if (splitRepo.length != 2) {
      templateRenderer.renderToStderr(INVALID_TEMPLATE, ImmutableMap.of("repo", repository));
      context.exit(2);
    }
    Repository repo = manager.get(new NamespaceAndName(splitRepo[0], splitRepo[1]));
    if (repo != null) {
      templateRenderer.renderToStdout(DEFAULT_TEMPLATE, ImmutableMap.of("repo", mapper.map(repo)));
    } else {
      templateRenderer.renderToStderr(NOT_FOUND_TEMPLATE, ImmutableMap.of("repo", repository));
      context.exit(1);
    }
  }
}
