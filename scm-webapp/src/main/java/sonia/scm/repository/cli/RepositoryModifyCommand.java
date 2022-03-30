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
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import javax.validation.constraints.Email;

@ParentCommand(value = RepositoryCommand.class)
@CommandLine.Command(name = "modify")
public class RepositoryModifyCommand implements Runnable {

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0")
  private String repository;
  @CommandLine.Option(names = {"--description", "-d"}, descriptionKey = "scm.repo.create.desc")
  private String description;
  @Email
  @CommandLine.Option(names = {"--contact", "-c"})
  private String contact;

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final RepositoryManager manager;

  @Inject
  RepositoryModifyCommand(RepositoryTemplateRenderer templateRenderer, CommandValidator validator, RepositoryManager manager) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.manager = manager;
  }

  @VisibleForTesting
  public void setRepository(String repository) {
    this.repository = repository;
  }

  @Override
  public void run() {
    validator.validate();
    String[] splitRepo = repository.split("/");
    if (splitRepo.length != 2) {
      templateRenderer.renderInvalidInputError();
    }
    Repository repo = manager.get(new NamespaceAndName(splitRepo[0], splitRepo[1]));

    if (repo != null) {
      if (contact != null) {
        repo.setContact(contact);
      }
      if (description != null) {
        repo.setDescription(description);
      }

      manager.modify(repo);
      templateRenderer.render(repo);
    } else {
      templateRenderer.renderNotFoundError();
    }
  }
}
