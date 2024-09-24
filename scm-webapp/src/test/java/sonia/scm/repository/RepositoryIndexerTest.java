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

    verify(index).store(idFor(heartOfGold), RepositoryPermissions.read(heartOfGold).asShiroString(), heartOfGold);
  }

  @Test
  void shouldDeleteRepositoryById() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();

    when(index.getDetails().getType()).then(ic -> Repository.class);
    indexer.createDeleteTask(heartOfGold).update(index);

    verify(index.delete()).byId(Id.of(Repository.class, heartOfGold).and(heartOfGold));
  }

  @Test
  void shouldDeleteAllByRepository() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();

    when(index.getDetails().getType()).then(ic -> String.class);
    indexer.createDeleteTask(heartOfGold).update(index);

    verify(index.delete().by(Repository.class, heartOfGold)).execute();
  }

  @Test
  void shouldReIndexAll() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    Repository puzzle = RepositoryTestData.create42Puzzle();
    when(repositoryManager.getAll()).thenReturn(Arrays.asList(heartOfGold, puzzle));

    RepositoryIndexer.ReIndexAll reIndexAll = new RepositoryIndexer.ReIndexAll(indexLogStore, repositoryManager);
    reIndexAll.update(index);

    verify(index.delete()).all();
    verify(index).store(idFor(heartOfGold), RepositoryPermissions.read(heartOfGold).asShiroString(), heartOfGold);
    verify(index).store(idFor(puzzle), RepositoryPermissions.read(puzzle).asShiroString(), puzzle);
  }

  @Test
  void shouldHandleDeleteEvents() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    RepositoryEvent event = new RepositoryEvent(HandlerEventType.DELETE, heartOfGold);

    indexer.handleEvent(event);

    verify(searchEngine.forIndices().forResource(heartOfGold)).batch(captor.capture());
    captor.getValue().update(index);

    verify(index.delete().by(Repository.class, heartOfGold)).execute();
  }

  @Test
  void shouldHandleUpdateEvents() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    RepositoryEvent event = new RepositoryEvent(HandlerEventType.CREATE, heartOfGold);

    indexer.handleEvent(event);

    verify(searchEngine.forType(Repository.class)).update(captor.capture());
    captor.getValue().update(index);
    verify(index).store(idFor(heartOfGold), RepositoryPermissions.read(heartOfGold).asShiroString(), heartOfGold);
  }

  private Id<Repository> idFor(Repository heartOfGold) {
    return Id.of(Repository.class, heartOfGold).and(heartOfGold);
  }
}
