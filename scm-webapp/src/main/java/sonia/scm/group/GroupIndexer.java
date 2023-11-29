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
