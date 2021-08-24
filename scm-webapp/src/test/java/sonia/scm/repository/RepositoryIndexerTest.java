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

package sonia.scm.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexLogStore;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SerializableIndexTask;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryIndexerTest {

  @Mock
  private RepositoryManager repositoryManager;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SearchEngine searchEngine;

  @InjectMocks
  private RepositoryIndexer indexer;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Index<Repository> index;

  @Mock
  private IndexLogStore indexLogStore;

  @Captor
  private ArgumentCaptor<SerializableIndexTask<Repository>> captor;

  @Test
  void shouldReturnRepositoryClass() {
    assertThat(indexer.getType()).isEqualTo(Repository.class);
  }

  @Test
  void shouldReturnVersion() {
    assertThat(indexer.getVersion()).isEqualTo(RepositoryIndexer.VERSION);
  }

  @Test
  void shouldReturnReIndexAllClass() {
    assertThat(indexer.getReIndexAllTask()).isEqualTo(RepositoryIndexer.ReIndexAll.class);
  }

  @Test
  void shouldCreateRepository() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();

    indexer.createStoreTask(heartOfGold).update(index);

    verify(index).store(Id.of(heartOfGold), RepositoryPermissions.read(heartOfGold).asShiroString(), heartOfGold);
  }

  @Test
  void shouldDeleteRepository() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();

    indexer.createDeleteTask(heartOfGold).update(index);

    verify(index.delete()).byRepository(heartOfGold);
  }

  @Test
  void shouldReIndexAll() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    Repository puzzle = RepositoryTestData.create42Puzzle();
    when(repositoryManager.getAll()).thenReturn(Arrays.asList(heartOfGold, puzzle));

    RepositoryIndexer.ReIndexAll reIndexAll = new RepositoryIndexer.ReIndexAll(indexLogStore, repositoryManager);
    reIndexAll.update(index);

    verify(index.delete()).all();
    verify(index).store(Id.of(heartOfGold), RepositoryPermissions.read(heartOfGold).asShiroString(), heartOfGold);
    verify(index).store(Id.of(puzzle), RepositoryPermissions.read(puzzle).asShiroString(), puzzle);
  }

  @Test
  void shouldHandleEvents() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    RepositoryEvent event = new RepositoryEvent(HandlerEventType.DELETE, heartOfGold);

    indexer.handleEvent(event);

    verify(searchEngine.forType(Repository.class)).update(captor.capture());
    captor.getValue().update(index);
    verify(index.delete()).byRepository(heartOfGold);
  }

}
