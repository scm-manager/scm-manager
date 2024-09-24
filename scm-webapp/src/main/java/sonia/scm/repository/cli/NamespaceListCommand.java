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

import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.RepositoryManager;

import static java.util.Collections.emptyMap;

@ParentCommand(value = NamespaceCommand.class)
@CommandLine.Command(name = "list", aliases = "ls")
class NamespaceListCommand implements Runnable {


  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final RepositoryManager manager;

  @Inject
  public NamespaceListCommand(RepositoryManager manager, TemplateRenderer templateRenderer, RepositoryToRepositoryCommandDtoMapper mapper) {
    this.manager = manager;
    this.templateRenderer = templateRenderer;
  }

  @Override
  public void run() {
    manager.getAllNamespaces()
      .forEach(this::render);
  }

  private void render(String namespace) {
    templateRenderer.renderToStdout(namespace + "\n", emptyMap());
  }
}
