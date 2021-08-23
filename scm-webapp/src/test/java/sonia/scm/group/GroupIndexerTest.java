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
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryIndexer;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexLogStore;
import sonia.scm.search.IndexTask;
import sonia.scm.search.SearchEngine;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupIndexerTest {

  @Mock
  private GroupManager groupManager;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SearchEngine searchEngine;

  @InjectMocks
  private GroupIndexer indexer;


  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Index<Group> index;

  @Mock
  private IndexLogStore indexLogStore;

  @Captor
  private ArgumentCaptor<IndexTask<Group>> captor;

  private final Group astronauts = new Group("xml", "astronauts");
  private final Group planetCreators = new Group("xml", "planet-creators");

  @Test
  void shouldReturnClass() {
    assertThat(indexer.getType()).isEqualTo(Group.class);
  }

  @Test
  void shouldReturnVersion() {
    assertThat(indexer.getVersion()).isEqualTo(GroupIndexer.VERSION);
  }


  @Test
  void shouldReturnReIndexAllClass() {
    assertThat(indexer.getReIndexAllTask()).isEqualTo(GroupIndexer.ReIndexAll.class);
  }

  @Test
  void shouldCreateGroup() {
    indexer.createStoreTask(astronauts).update(index);

    verify(index).store(Id.of(astronauts), GroupPermissions.read(astronauts).asShiroString(), astronauts);
  }

  @Test
  void shouldDeleteGroup() {
    indexer.createDeleteTask(astronauts).update(index);

    verify(index.delete()).byId(Id.of(astronauts));
  }

  @Test
  void shouldReIndexAll() {
    when(groupManager.getAll()).thenReturn(Arrays.asList(astronauts, planetCreators));

    GroupIndexer.ReIndexAll reIndexAll = new GroupIndexer.ReIndexAll(indexLogStore, groupManager);
    reIndexAll.update(index);

    verify(index.delete()).all();
    verify(index).store(Id.of(astronauts), GroupPermissions.read(astronauts).asShiroString(), astronauts);
    verify(index).store(Id.of(planetCreators), GroupPermissions.read(planetCreators).asShiroString(), planetCreators);
  }

  @Test
  void shouldHandleEvents() {
    GroupEvent event = new GroupEvent(HandlerEventType.DELETE, astronauts);

    indexer.handleEvent(event);

    verify(searchEngine.forType(Group.class)).update(captor.capture());
    captor.getValue().update(index);
    verify(index.delete()).byId(Id.of(astronauts));
  }

}
