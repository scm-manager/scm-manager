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

package sonia.scm.user;

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
class UserIndexerTest {

  @Mock
  private UserManager userManager;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SearchEngine searchEngine;

  @InjectMocks
  private UserIndexer indexer;


  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Index<User> index;

  @Mock
  private IndexLogStore indexLogStore;

  @Captor
  private ArgumentCaptor<SerializableIndexTask<User>> captor;

  @Test
  void shouldReturnType() {
    assertThat(indexer.getType()).isEqualTo(User.class);
  }

  @Test
  void shouldReturnVersion() {
    assertThat(indexer.getVersion()).isEqualTo(UserIndexer.VERSION);
  }


  @Test
  void shouldReturnReIndexAllClass() {
    assertThat(indexer.getReIndexAllTask()).isEqualTo(UserIndexer.ReIndexAll.class);
  }

  @Test
  void shouldCreateUser() {
    User trillian = UserTestData.createTrillian();

    indexer.createStoreTask(trillian).update(index);

    verify(index).store(Id.of(User.class, trillian), UserPermissions.read(trillian).asShiroString(), trillian);
  }

  @Test
  void shouldDeleteUser() {
    User trillian = UserTestData.createTrillian();

    indexer.createDeleteTask(trillian).update(index);

    verify(index.delete()).byId(Id.of(User.class, trillian));
  }

  @Test
  void shouldReIndexAll() {
    User trillian = UserTestData.createTrillian();
    User slarti = UserTestData.createSlarti();
    when(userManager.getAll()).thenReturn(Arrays.asList(trillian, slarti));

    UserIndexer.ReIndexAll reIndexAll = new UserIndexer.ReIndexAll(indexLogStore, userManager);
    reIndexAll.update(index);

    verify(index.delete()).all();
    verify(index).store(Id.of(User.class, trillian), UserPermissions.read(trillian).asShiroString(), trillian);
    verify(index).store(Id.of(User.class, slarti), UserPermissions.read(slarti).asShiroString(), slarti);
  }

  @Test
  void shouldHandleEvents() {
    User trillian = UserTestData.createTrillian();
    UserEvent event = new UserEvent(HandlerEventType.DELETE, trillian);

    indexer.handleEvent(event);

    verify(searchEngine.forType(User.class)).update(captor.capture());
    captor.getValue().update(index);
    verify(index.delete()).byId(Id.of(User.class, trillian));
  }

}
