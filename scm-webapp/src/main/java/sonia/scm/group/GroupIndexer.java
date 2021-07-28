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
import sonia.scm.plugin.Extension;
import sonia.scm.search.HandlerEventIndexSyncer;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexNames;
import sonia.scm.search.IndexQueue;
import sonia.scm.search.Indexer;

import javax.inject.Inject;
import javax.inject.Singleton;

@Extension
@Singleton
public class GroupIndexer implements Indexer<Group> {

  @VisibleForTesting
  static final String INDEX = IndexNames.DEFAULT;
  @VisibleForTesting
  static final int VERSION = 1;

  private final GroupManager groupManager;
  private final IndexQueue indexQueue;

  @Inject
  public GroupIndexer(GroupManager groupManager, IndexQueue indexQueue) {
    this.groupManager = groupManager;
    this.indexQueue = indexQueue;
  }

  @Override
  public Class<Group> getType() {
    return Group.class;
  }

  @Override
  public String getIndex() {
    return INDEX;
  }

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Subscribe(async = false)
  public void handleEvent(GroupEvent event) {
    HandlerEventIndexSyncer.handleEvent(this, event);
  }

  @Override
  public Updater<Group> open() {
    return new GroupIndexUpdater(groupManager, indexQueue.getQueuedIndex(INDEX));
  }

  public static class GroupIndexUpdater implements Updater<Group> {

    private final GroupManager groupManager;
    private final Index index;

    private GroupIndexUpdater(GroupManager groupManager, Index index) {
      this.groupManager = groupManager;
      this.index = index;
    }

    @Override
    public void store(Group group) {
      index.store(Id.of(group), GroupPermissions.read(group).asShiroString(), group);
    }

    @Override
    public void delete(Group group) {
      index.delete(Id.of(group), Group.class);
    }

    @Override
    public void reIndexAll() {
      index.deleteByType(Group.class);
      for (Group group : groupManager.getAll()) {
        store(group);
      }
    }

    @Override
    public void close() {
      index.close();
    }
  }
}
