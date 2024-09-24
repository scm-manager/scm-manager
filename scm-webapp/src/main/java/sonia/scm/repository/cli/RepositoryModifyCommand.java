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
import jakarta.inject.Inject;
import jakarta.validation.constraints.Email;
import picocli.CommandLine;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

@ParentCommand(value = RepositoryCommand.class)
@CommandLine.Command(name = "modify")
class RepositoryModifyCommand implements Runnable {

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final RepositoryManager manager;

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0", descriptionKey = "scm.repo.modify.repository")
  private String repository;

  @CommandLine.Option(names = {"--description", "-d"}, descriptionKey = "scm.repo.create.desc")
  private String description;

  @Email
  @CommandLine.Option(names = {"--contact", "-c"})
  private String contact;


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
    if (splitRepo.length == 2) {
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
    } else {
      templateRenderer.renderInvalidInputError();
    }
  }
}
