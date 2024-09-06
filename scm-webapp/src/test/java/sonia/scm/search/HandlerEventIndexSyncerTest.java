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
