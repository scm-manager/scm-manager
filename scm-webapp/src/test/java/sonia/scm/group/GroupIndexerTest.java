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

package sonia.scm.group;

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
  private ArgumentCaptor<SerializableIndexTask<Group>> captor;

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

    verify(index).store(Id.of(Group.class, astronauts), GroupPermissions.read(astronauts).asShiroString(), astronauts);
  }

  @Test
  void shouldDeleteGroup() {
    indexer.createDeleteTask(astronauts).update(index);

    verify(index.delete()).byId(Id.of(Group.class, astronauts));
  }

  @Test
  void shouldReIndexAll() {
    when(groupManager.getAll()).thenReturn(Arrays.asList(astronauts, planetCreators));

    GroupIndexer.ReIndexAll reIndexAll = new GroupIndexer.ReIndexAll(indexLogStore, groupManager);
    reIndexAll.update(index);

    verify(index.delete()).all();
    verify(index).store(Id.of(Group.class, astronauts), GroupPermissions.read(astronauts).asShiroString(), astronauts);
    verify(index).store(Id.of(Group.class, planetCreators), GroupPermissions.read(planetCreators).asShiroString(), planetCreators);
  }

  @Test
  void shouldHandleEvents() {
    GroupEvent event = new GroupEvent(HandlerEventType.DELETE, astronauts);

    indexer.handleEvent(event);

    verify(searchEngine.forType(Group.class)).update(captor.capture());
    captor.getValue().update(index);
    verify(index.delete()).byId(Id.of(Group.class, astronauts));
  }

}
