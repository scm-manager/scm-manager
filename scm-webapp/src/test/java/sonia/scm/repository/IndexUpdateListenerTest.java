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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexLog;
import sonia.scm.search.IndexLogStore;
import sonia.scm.search.IndexNames;
import sonia.scm.search.IndexQueue;
import sonia.scm.web.security.AdministrationContext;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexUpdateListenerTest {

  @Mock
  private RepositoryManager repositoryManager;

  @Mock
  private AdministrationContext administrationContext;

  @Mock
  private IndexQueue indexQueue;

  @Mock
  private Index index;

  @Mock
  private IndexLogStore indexLogStore;

  @InjectMocks
  private IndexUpdateListener updateListener;

  @Test
  @SuppressWarnings("java:S6068")
    // eq is required for verify with changing parametern
  void shouldIndexAllRepositories() {
    when(indexLogStore.get(IndexNames.DEFAULT, Repository.class)).thenReturn(Optional.empty());
    doAnswer(ic -> {
      IndexUpdateListener.ReIndexAll reIndexAll = new IndexUpdateListener.ReIndexAll(repositoryManager, indexQueue);
      reIndexAll.run();
      return null;
    })
      .when(administrationContext)
      .runAsAdmin(IndexUpdateListener.ReIndexAll.class);

    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    Repository puzzle42 = RepositoryTestData.create42Puzzle();
    List<Repository> repositories = ImmutableList.of(heartOfGold, puzzle42);

    when(repositoryManager.getAll()).thenReturn(repositories);
    when(indexQueue.getQueuedIndex(IndexNames.DEFAULT)).thenReturn(index);

    updateListener.contextInitialized(null);

    verify(index).store(eq(Id.of(heartOfGold)), eq(RepositoryPermissions.read(heartOfGold).asShiroString()), eq(heartOfGold));
    verify(index).store(eq(Id.of(puzzle42)), eq(RepositoryPermissions.read(puzzle42).asShiroString()), eq(puzzle42));
    verify(index).close();

    verify(indexLogStore).log(IndexNames.DEFAULT, Repository.class, IndexUpdateListener.INDEX_VERSION);
  }

  @Test
  void shouldSkipReIndex() {
    IndexLog log = new IndexLog(1);
    when(indexLogStore.get(IndexNames.DEFAULT, Repository.class)).thenReturn(Optional.of(log));

    updateListener.contextInitialized(null);

    verifyNoInteractions(indexQueue);
  }

  @ParameterizedTest
  @EnumSource(value = HandlerEventType.class, mode = EnumSource.Mode.MATCH_ANY, names = "BEFORE_.*")
  void shouldIgnoreBeforeEvents(HandlerEventType type) {
    RepositoryEvent event = new RepositoryEvent(type, RepositoryTestData.create42Puzzle());

    updateListener.handleEvent(event);

    verifyNoInteractions(indexQueue);
  }

  @ParameterizedTest
  @EnumSource(value = HandlerEventType.class, mode = EnumSource.Mode.INCLUDE, names = {"CREATE", "MODIFY"})
  void shouldStore(HandlerEventType type) {
    when(indexQueue.getQueuedIndex(IndexNames.DEFAULT)).thenReturn(index);

    Repository puzzle = RepositoryTestData.create42Puzzle();
    RepositoryEvent event = new RepositoryEvent(type, puzzle);

    updateListener.handleEvent(event);

    verify(index).store(Id.of(puzzle), RepositoryPermissions.read(puzzle).asShiroString(), puzzle);
    verify(index).close();
  }

  @Test
  void shouldDelete() {
    when(indexQueue.getQueuedIndex(IndexNames.DEFAULT)).thenReturn(index);
    Repository puzzle = RepositoryTestData.create42Puzzle();
    RepositoryEvent event = new RepositoryEvent(HandlerEventType.DELETE, puzzle);

    updateListener.handleEvent(event);

    verify(index).deleteByRepository(puzzle.getId());
    verify(index).close();
  }

}
