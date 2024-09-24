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
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import java.util.Collections;

@CommandLine.Command(name = "delete", aliases = "rm")
@ParentCommand(RepositoryCommand.class)
class RepositoryDeleteCommand implements Runnable {

  private static final String PROMPT_TEMPLATE = "{{i18n.repoDeletePrompt}}";

  @CommandLine.Parameters(descriptionKey = "scm.repo.delete.repository", paramLabel = "namespace/name")
  private String repository;

  @CommandLine.Option(names = {"--yes", "-y"}, descriptionKey = "scm.repo.delete.prompt")
  private boolean shouldDelete;

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  private final RepositoryManager manager;

  @Inject
  public RepositoryDeleteCommand(RepositoryManager manager, RepositoryTemplateRenderer templateRenderer) {
    this.manager = manager;
    this.templateRenderer = templateRenderer;
  }

  @Override
  public void run() {
    if (!shouldDelete) {
      templateRenderer.renderToStderr(PROMPT_TEMPLATE, Collections.emptyMap());
      return;
    }
    String[] splitRepo = repository.split("/");
    if (splitRepo.length == 2) {
      Repository repo = manager.get(new NamespaceAndName(splitRepo[0], splitRepo[1]));
      if (repo != null) {
        manager.delete(repo);
      }
    } else {
      templateRenderer.renderInvalidInputError();
    }
  }

  @VisibleForTesting
  void setRepository(String repository) {
    this.repository = repository;
  }

  @VisibleForTesting
  void setShouldDelete(boolean shouldDelete) {
    this.shouldDelete = shouldDelete;
  }
}
