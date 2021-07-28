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

package sonia.scm.group;

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
class GroupIndexerTest {

  @Mock
  private GroupManager groupManager;

  @Mock
  private IndexQueue indexQueue;

  @InjectMocks
  private GroupIndexer indexer;

  @Test
  void shouldReturnRepositoryClass() {
    assertThat(indexer.getType()).isEqualTo(Group.class);
  }

  @Test
  void shouldReturnIndexName() {
    assertThat(indexer.getIndex()).isEqualTo(GroupIndexer.INDEX);
  }

  @Test
  void shouldReturnVersion() {
    assertThat(indexer.getVersion()).isEqualTo(GroupIndexer.VERSION);
  }

  @Nested
  class UpdaterTests {

    @Mock
    private Index index;

    private final Group group = new Group("xml", "astronauts");

    @BeforeEach
    void open() {
      when(indexQueue.getQueuedIndex(GroupIndexer.INDEX)).thenReturn(index);
    }

    @Test
    void shouldStoreRepository() {
      indexer.open().store(group);

      verify(index).store(Id.of(group), "group:read:astronauts", group);
    }

    @Test
    void shouldDeleteByRepository() {
      indexer.open().delete(group);

      verify(index).delete(Id.of(group), Group.class);
    }

    @Test
    void shouldReIndexAll() {
      when(groupManager.getAll()).thenReturn(singletonList(group));

      indexer.open().reIndexAll();

      verify(index).deleteByType(Group.class);
      verify(index).store(Id.of(group), "group:read:astronauts", group);
    }

    @Test
    void shouldHandleEvent() {
      GroupEvent event = new GroupEvent(HandlerEventType.DELETE, group);

      indexer.handleEvent(event);

      verify(index).delete(Id.of(group), Group.class);
    }

    @Test
    void shouldCloseIndex() {
      indexer.open().close();

      verify(index).close();
    }
  }

}
