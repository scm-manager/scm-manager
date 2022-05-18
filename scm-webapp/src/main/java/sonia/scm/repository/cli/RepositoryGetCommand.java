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
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;

@ParentCommand(value = RepositoryCommand.class)
@CommandLine.Command(name = "get")
class RepositoryGetCommand implements Runnable {

  @CommandLine.Parameters(paramLabel = "namespace/name", descriptionKey = "scm.repo.create.repository", index = "0")
  private String repository;

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  private final RepositoryManager manager;

  @Inject
  RepositoryGetCommand(RepositoryTemplateRenderer templateRenderer, RepositoryManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @VisibleForTesting
  void setRepository(String repository) {
    this.repository = repository;
  }

  @Override
  public void run() {
    String[] splitRepo = repository.split("/");
    if (splitRepo.length == 2) {
      Repository repo = manager.get(new NamespaceAndName(splitRepo[0], splitRepo[1]));
      if (repo != null) {
        templateRenderer.render(repo);
      } else {
        templateRenderer.renderNotFoundError();
      }
    } else {
      templateRenderer.renderInvalidInputError();
    }
  }
}
