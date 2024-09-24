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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryGetCommandTest {

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryTemplateRenderer templateRenderer;

  @InjectMocks
  private RepositoryGetCommand command;

  @Test
  void shouldRenderNotFoundError() {
    String repo = "test/repo";

    when(repositoryManager.get(new NamespaceAndName("test", "repo"))).thenReturn(null);

    command.setRepository(repo);
    command.run();

    verify(templateRenderer).renderNotFoundError();
  }

  @Test
  void shouldRenderInvalidInputError() {
    String repo = "repo";

    command.setRepository(repo);
    command.run();

    verify(templateRenderer).renderInvalidInputError();
  }

  @Test
  void shouldRenderTemplateToStdout() {
    String repo = "test/repo";
    Repository puzzle = RepositoryTestData.create42Puzzle();
    when(repositoryManager.get(new NamespaceAndName("test", "repo"))).thenReturn(puzzle);

    command.setRepository(repo);
    command.run();

    verify(templateRenderer).render(puzzle);
  }

}
