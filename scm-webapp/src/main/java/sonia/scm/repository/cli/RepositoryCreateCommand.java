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
import jakarta.validation.constraints.Email;
import picocli.CommandLine;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryInitializer;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryName;
import sonia.scm.repository.RepositoryTypeConstraint;

@CommandLine.Command(name = "create")
@ParentCommand(value = RepositoryCommand.class)
class RepositoryCreateCommand implements Runnable {

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final RepositoryManager manager;
  private final RepositoryInitializer repositoryInitializer;

  @RepositoryTypeConstraint
  @CommandLine.Parameters(descriptionKey = "scm.repo.create.type")
  private String type;

  @RepositoryName(namespace = RepositoryName.Namespace.OPTIONAL)
  @CommandLine.Parameters(descriptionKey = "scm.repo.create.repository", paramLabel = "<name>")
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
