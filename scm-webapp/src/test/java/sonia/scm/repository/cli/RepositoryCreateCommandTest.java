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
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryInitializer;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryCreateCommandTest {

  @Mock
  private RepositoryManager manager;
  @Mock
  private RepositoryInitializer initializer;
  @Mock
  private RepositoryTemplateRenderer templateRenderer;
  @Mock
  private CommandValidator commandValidator;

  @InjectMocks
  private RepositoryCreateCommand command;

  @Test
  void shouldValidate() {
    command.setType("git");
    command.setRepository("test/repo");
    command.run();

    verify(commandValidator).validate();
  }

  @Test
  void shouldCreateRepoWithoutInit() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    command.setType(heartOfGold.getType());
    command.setRepository(heartOfGold.getNamespaceAndName().toString());
    command.run();

    verify(manager).create(argThat(repository -> {
      assertThat(repository.getType()).isEqualTo(heartOfGold.getType());
      return true;
    }));
    verify(initializer, never()).initialize(eq(heartOfGold), anyMap());
  }

  @Test
  void shouldCreateRepoWithInit() {
    Repository puzzle = RepositoryTestData.create42Puzzle();
    when(manager.create(any())).thenReturn(puzzle);
    command.setType(puzzle.getType());
    command.setRepository(puzzle.getNamespaceAndName().toString());
    command.setInit(true);
    command.run();

    verify(initializer).initialize(eq(puzzle), anyMap());
  }

  @Test
  void shouldRenderTemplateAfterCreation() {
    Repository puzzle = RepositoryTestData.create42Puzzle();
    when(manager.create(any())).thenReturn(puzzle);

    command.setType(puzzle.getType());
    command.setRepository(puzzle.getNamespaceAndName().toString());
    command.run();

    verify(templateRenderer).render(puzzle);
  }
}
