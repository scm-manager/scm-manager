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

package sonia.scm.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandlerEventIndexSyncerTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SearchEngine searchEngine;

  @Mock
  private Indexer<Repository> indexer;

  @BeforeEach
  void setUpIndexer() {
    lenient().when(indexer.getType()).thenReturn(Repository.class);
  }

  @ParameterizedTest
  @EnumSource(value = HandlerEventType.class, mode = EnumSource.Mode.MATCH_ANY, names = "BEFORE_.*")
  void shouldIgnoreBeforeEvents(HandlerEventType type) {
    RepositoryEvent event = new RepositoryEvent(type, RepositoryTestData.create42Puzzle());

    new HandlerEventIndexSyncer<>(searchEngine, indexer).handleEvent(event);

    verifyNoInteractions(indexer);
  }

  @ParameterizedTest
  @EnumSource(value = HandlerEventType.class, mode = EnumSource.Mode.INCLUDE, names = {"CREATE", "MODIFY"})
  void shouldStore(HandlerEventType type) {
    SerializableIndexTask<Repository> store = index -> {};

    Repository puzzle = RepositoryTestData.create42Puzzle();
    when(indexer.createStoreTask(puzzle)).thenReturn(store);

    RepositoryEvent event = new RepositoryEvent(type, puzzle);

    new HandlerEventIndexSyncer<>(searchEngine, indexer).handleEvent(event);

    verify(searchEngine.forType(Repository.class)).update(store);
  }

  @Test
  void shouldDelete() {
    SerializableIndexTask<Repository> delete = index -> {};

    Repository puzzle = RepositoryTestData.create42Puzzle();
    when(indexer.createDeleteTask(puzzle)).thenReturn(delete);

    RepositoryEvent event = new RepositoryEvent(HandlerEventType.DELETE, puzzle);
    new HandlerEventIndexSyncer<>(searchEngine, indexer).handleEvent(event);

    verify(searchEngine.forType(Repository.class)).update(delete);
  }

}
