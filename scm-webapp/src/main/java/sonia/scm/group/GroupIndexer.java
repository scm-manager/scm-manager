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

import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.plugin.Extension;
import sonia.scm.search.HandlerEventIndexSyncer;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexLogStore;
import sonia.scm.search.Indexer;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SerializableIndexTask;

@Extension
@Singleton
public class GroupIndexer implements Indexer<Group> {

  @VisibleForTesting
  static final int VERSION = 1;

  private final SearchEngine searchEngine;

  @Inject
  public GroupIndexer(SearchEngine searchEngine) {
    this.searchEngine = searchEngine;
  }

  @Override
  public Class<Group> getType() {
    return Group.class;
  }

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public Class<? extends ReIndexAllTask<Group>> getReIndexAllTask() {
    return ReIndexAll.class;
  }

  @Override
  public SerializableIndexTask<Group> createStoreTask(Group group) {
    return index -> store(index, group);
  }

  @Override
  public SerializableIndexTask<Group> createDeleteTask(Group group) {
    return index -> index.delete().byId(Id.of(Group.class, group));
  }

  @Subscribe(async = false)
  public void handleEvent(GroupEvent event) {
    new HandlerEventIndexSyncer<>(searchEngine, this).handleEvent(event);
  }

  public static void store(Index<Group> index, Group group) {
    index.store(Id.of(Group.class, group), GroupPermissions.read(group).asShiroString(), group);
  }

  public static class ReIndexAll extends ReIndexAllTask<Group> {

    private final GroupManager groupManager;

    @Inject
    public ReIndexAll(IndexLogStore logStore, GroupManager groupManager) {
      super(logStore, Group.class, VERSION);
      this.groupManager = groupManager;
    }

    @Override
    public void update(Index<Group> index) {
      index.delete().all();
      for (Group group : groupManager.getAll()) {
        store(index, group);
      }
    }
  }

}
