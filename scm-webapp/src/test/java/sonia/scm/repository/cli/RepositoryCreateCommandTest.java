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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CommandValidator;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryInitializer;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import javax.inject.Provider;

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
  @Mock
  private Provider<NamespaceStrategy> namespaceStrategyProvider;

  @InjectMocks
  private RepositoryCreateCommand command;

  @BeforeEach
  void initCommand() {
    when(namespaceStrategyProvider.get()).thenReturn(repository -> "test");
  }

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
      assertThat(repository.getNamespaceAndName().toString()).hasToString(heartOfGold.getNamespaceAndName().toString());
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
