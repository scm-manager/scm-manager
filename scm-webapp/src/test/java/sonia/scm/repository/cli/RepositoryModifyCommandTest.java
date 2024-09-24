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
import sonia.scm.cli.CommandValidator;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryModifyCommandTest {

  @Mock
  private RepositoryTemplateRenderer templateRenderer;
  @Mock
  private CommandValidator validator;
  @Mock
  private RepositoryManager manager;

  @InjectMocks
  private RepositoryModifyCommand command;

  @Test
  void shouldValidateParameters() {
    command.setRepository("test/repo");
    command.run();

    verify(validator).validate();
  }

  @Test
  void shouldRenderInvalidInputError() {
    command.setRepository("test");
    command.run();

    verify(templateRenderer).renderInvalidInputError();
  }

  @Test
  void shouldRenderNotFoundError() {
    when(manager.get(new NamespaceAndName("test", "repo"))).thenReturn(null);
    command.setRepository("test/repo");
    command.run();

    verify(templateRenderer).renderNotFoundError();
  }

  @Test
  void shouldModifyRepository() {
    Repository puzzle = RepositoryTestData.create42Puzzle();
    when(manager.get(new NamespaceAndName("test", "repo"))).thenReturn(puzzle);
    command.setRepository("test/repo");
    command.run();

    verify(manager).modify(puzzle);
    verify(templateRenderer).render(puzzle);
  }

}
