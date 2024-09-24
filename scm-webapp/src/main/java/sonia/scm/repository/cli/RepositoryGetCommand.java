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

import com.cronutils.utils.VisibleForTesting;
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

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
