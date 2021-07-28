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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexBootstrapListenerTest {

  @Mock
  private AdministrationContext administrationContext;

  @Mock
  private IndexLogStore indexLogStore;

  @Test
  void shouldReIndexWithoutLog() {
    mockAdminContext();
    Indexer<Repository> indexer = indexer(Repository.class, 1);
    Indexer.Updater<Repository> updater = updater(indexer);

    mockEmptyIndexLog(Repository.class);
    doInitialization(indexer);

    verify(updater).reIndexAll();
    verify(updater).close();
    verify(indexLogStore).log(IndexNames.DEFAULT, Repository.class, 1);
  }

  @Test
  void shouldReIndexIfVersionWasUpdated() {
    mockAdminContext();
    Indexer<User> indexer = indexer(User.class, 2);
    Indexer.Updater<User> updater = updater(indexer);

    mockIndexLog(User.class, 1);
    doInitialization(indexer);

    verify(updater).reIndexAll();
    verify(updater).close();
    verify(indexLogStore).log(IndexNames.DEFAULT, User.class, 2);
  }

  @Test
  void shouldSkipReIndexIfVersionIsEqual() {
    Indexer<Group> indexer = indexer(Group.class, 3);

    mockIndexLog(Group.class, 3);
    doInitialization(indexer);

    verify(indexer, never()).open();
  }

  private <T> void mockIndexLog(Class<T> type, int version) {
    mockIndexLog(type, new IndexLog(version));
  }

  private <T> void mockEmptyIndexLog(Class<T> type) {
    mockIndexLog(type, null);
  }

  private <T> void mockIndexLog(Class<T> type, @Nullable IndexLog indexLog) {
    when(indexLogStore.get(IndexNames.DEFAULT, type)).thenReturn(Optional.ofNullable(indexLog));
  }

  private void mockAdminContext() {
    doAnswer(ic -> {
      PrivilegedAction action = ic.getArgument(0);
      action.run();
      return null;
    }).when(administrationContext).runAsAdmin(any(PrivilegedAction.class));
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
      administrationContext, indexLogStore, new HashSet<>(Arrays.asList(indexers))
    );
  }

  private <T> Indexer<T> indexer(Class<T> type, int version) {
    Indexer<T> indexer = mock(Indexer.class);
    when(indexer.getType()).thenReturn(type);
    when(indexer.getVersion()).thenReturn(version);
    when(indexer.getIndex()).thenReturn(IndexNames.DEFAULT);
    return indexer;
  }

  private <T> Indexer.Updater<T> updater(Indexer<T> indexer) {
    Indexer.Updater<T> updater = mock(Indexer.Updater.class);
    when(indexer.open()).thenReturn(updater);
    return updater;
  }

}
