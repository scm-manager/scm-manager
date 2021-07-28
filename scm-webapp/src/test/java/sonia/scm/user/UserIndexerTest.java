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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexQueue;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserIndexerTest {

  @Mock
  private UserManager userManager;

  @Mock
  private IndexQueue indexQueue;

  @InjectMocks
  private UserIndexer indexer;

  @Test
  void shouldReturnRepositoryClass() {
    assertThat(indexer.getType()).isEqualTo(User.class);
  }

  @Test
  void shouldReturnIndexName() {
    assertThat(indexer.getIndex()).isEqualTo(UserIndexer.INDEX);
  }

  @Test
  void shouldReturnVersion() {
    assertThat(indexer.getVersion()).isEqualTo(UserIndexer.VERSION);
  }

  @Nested
  class UpdaterTests {

    @Mock
    private Index index;

    private final User user = UserTestData.createTrillian();

    @BeforeEach
    void open() {
      when(indexQueue.getQueuedIndex(UserIndexer.INDEX)).thenReturn(index);
    }

    @Test
    void shouldStoreRepository() {
      indexer.open().store(user);

      verify(index).store(Id.of(user), "user:read:trillian", user);
    }

    @Test
    void shouldDeleteByRepository() {
      indexer.open().delete(user);

      verify(index).delete(Id.of(user), User.class);
    }

    @Test
    void shouldReIndexAll() {
      when(userManager.getAll()).thenReturn(singletonList(user));

      indexer.open().reIndexAll();

      verify(index).deleteByType(User.class);
      verify(index).store(Id.of(user), "user:read:trillian", user);
    }

    @Test
    void shouldHandleEvent() {
      UserEvent event = new UserEvent(HandlerEventType.DELETE, user);

      indexer.handleEvent(event);

      verify(index).delete(Id.of(user), User.class);
    }

    @Test
    void shouldCloseIndex() {
      indexer.open().close();

      verify(index).close();
    }
  }

}
