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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryInitializer;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTypeConstraint;

import javax.inject.Inject;
import javax.validation.constraints.Email;

@CommandLine.Command(name = "create")
@ParentCommand(value = RepositoryCommand.class)
public class RepositoryCreateCommand implements Runnable {

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final RepositoryManager manager;
  private final RepositoryInitializer repositoryInitializer;

  @RepositoryTypeConstraint
  @CommandLine.Parameters(descriptionKey = "scm.repo.create.type")
  private String type;
  @CommandLine.Parameters(descriptionKey = "scm.repo.create.repository", paramLabel = "name")
  private String repository;
  @CommandLine.Option(names = {"--description", "-d"}, descriptionKey = "scm.repo.create.desc")
  private String description;
  @Email
  @CommandLine.Option(names = {"--contact", "-c"})
  private String contact;
  @CommandLine.Option(names = {"--init", "-i"}, descriptionKey = "scm.repo.create.init")
  private boolean init;

  @Inject
  public RepositoryCreateCommand(RepositoryTemplateRenderer templateRenderer,
                                 CommandValidator validator,
                                 RepositoryManager manager,
                                 RepositoryInitializer repositoryInitializer) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.manager = manager;
    this.repositoryInitializer = repositoryInitializer;
  }

  @Override
  public void run() {
    validator.validate();
    Repository newRepo = new Repository();
    String[] splitRepoName = repository.split("/");
    if (splitRepoName.length == 2) {
      newRepo.setNamespace(splitRepoName[0]);
      newRepo.setName(splitRepoName[1]);
    } else {
      newRepo.setName(repository);
    }
    newRepo.setType(type);
    newRepo.setDescription(description);
    newRepo.setContact(contact);
    Repository createdRepo = manager.create(newRepo);
    if (init) {
      repositoryInitializer.initialize(createdRepo, ImmutableMap.of());
    }
    templateRenderer.render(createdRepo);
  }

  @VisibleForTesting
  void setType(String type) {
    this.type = type;
  }

  @VisibleForTesting
  void setRepository(String repository) {
    this.repository = repository;
  }

  @VisibleForTesting
  void setInit(boolean init) {
    this.init = init;
  }
}
