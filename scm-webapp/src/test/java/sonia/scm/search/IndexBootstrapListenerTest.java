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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexBootstrapListenerTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SearchEngine searchEngine;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private IndexLogStore indexLogStore;

  @Test
  void shouldReIndexWithoutLog() {
    Indexer<Repository> indexer = indexer(Repository.class, 1);

    mockEmptyIndexLog(Repository.class);
    doInitialization(indexer);

    verify(searchEngine.forType(Repository.class)).update(RepositoryReIndexAllTask.class);
  }

  @Test
  void shouldReIndexIfVersionWasUpdated() {
    Indexer<User> indexer = indexer(User.class, 2);

    mockIndexLog(User.class, 1);
    doInitialization(indexer);

    verify(searchEngine.forType(User.class)).update(UserReIndexAllTask.class);
  }

  @Test
  void shouldSkipReIndexIfVersionIsEqual() {
    Indexer<Group> indexer = indexer(Group.class, 3);

    mockIndexLog(Group.class, 3);
    doInitialization(indexer);

    verifyNoInteractions(searchEngine);
  }

  private <T> void mockIndexLog(Class<T> type, int version) {
    mockIndexLog(type, new IndexLog(version));
  }

  private <T> void mockEmptyIndexLog(Class<T> type) {
    mockIndexLog(type, null);
  }

  private <T> void mockIndexLog(Class<T> type, @Nullable IndexLog indexLog) {
    when(indexLogStore.defaultIndex().get(type)).thenReturn(Optional.ofNullable(indexLog));
  }


  @SuppressWarnings("rawtypes")
  private void doInitialization(Indexer... indexers) {
    IndexBootstrapListener listener = listener(indexers);
    listener.contextInitialized(null);
  }

  @Nonnull
  @SuppressWarnings("rawtypes")
  private IndexBootstrapListener listener(Indexer... indexers) {
    return new IndexBootstrapListener(
      searchEngine, indexLogStore, new HashSet<>(Arrays.asList(indexers))
    );
  }

  @SuppressWarnings("unchecked")
  private <T> Indexer<T> indexer(Class<T> type, int version) {
    Indexer<T> indexer = mock(Indexer.class);
    when(indexer.getType()).thenReturn(type);
    lenient().when(indexer.getVersion()).thenReturn(version);
    lenient().when(indexer.getReIndexAllTask()).thenAnswer(ic -> {
      if (type == User.class) {
        return UserReIndexAllTask.class;
      }
      return RepositoryReIndexAllTask.class;
    });
    return indexer;
  }

  public static class RepositoryReIndexAllTask extends Indexer.ReIndexAllTask<Repository> {

    public RepositoryReIndexAllTask(IndexLogStore logStore, Class<Repository> type, int version) {
      super(logStore, type, version);
    }

    @Override
    public void update(Index<Repository> index) {

    }
  }

  public static class UserReIndexAllTask extends Indexer.ReIndexAllTask<User> {

    public UserReIndexAllTask(IndexLogStore logStore, Class<User> type, int version) {
      super(logStore, type, version);
    }

    @Override
    public void update(Index<User> index) {

    }
  }

}
