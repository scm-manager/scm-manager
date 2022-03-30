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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import sonia.scm.cli.CliContext;
import sonia.scm.cli.ExitCode;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import java.util.Collections;

@CommandLine.Command(name = "delete")
@ParentCommand(RepositoryCommand.class)
public class RepositoryDeleteCommand implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryDeleteCommand.class);

  private static final String PROMPT_TEMPLATE = "{{i18n.repoDeletePrompt}}";

  @CommandLine.Parameters
  private String repository;

  @CommandLine.Option(names = {"--yes", "-y"}, descriptionKey = "scm.repo.delete.prompt")
  private boolean shouldDelete;

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final RepositoryManager manager;
  private final CliContext context;

  @Inject
  public RepositoryDeleteCommand(RepositoryManager manager, CliContext context, TemplateRenderer templateRenderer) {
    this.manager = manager;
    this.context = context;
    this.templateRenderer = templateRenderer;
  }

  @Override
  public void run() {
    if (!shouldDelete) {
      templateRenderer.renderToStderr(PROMPT_TEMPLATE, Collections.emptyMap());
      return;
    }
    String[] splitRepo = repository.split("/");
    if (splitRepo.length != 2) {
      context.exit(ExitCode.INVALID_INPUT);
      return;
    }
    try {
      Repository repo = manager.get(new NamespaceAndName(splitRepo[0], splitRepo[1]));
      if (repo != null) {
        manager.delete(repo);
      }
    } catch (Exception e) {
      LOG.error("Could not delete repository", e);
      context.exit(ExitCode.SERVER_ERROR);
    }
  }
}
