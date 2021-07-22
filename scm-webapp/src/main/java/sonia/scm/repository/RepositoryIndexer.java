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

package sonia.scm.repository;

import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import sonia.scm.plugin.Extension;
import sonia.scm.search.HandlerEvents;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexNames;
import sonia.scm.search.IndexQueue;
import sonia.scm.search.Indexer;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Extension
public class RepositoryIndexer implements Indexer<Repository> {

  @VisibleForTesting
  static final int VERSION = 2;

  @VisibleForTesting
  static final String INDEX = IndexNames.DEFAULT;

  private final RepositoryManager repositoryManager;
  private final IndexQueue indexQueue;

  @Inject
  public RepositoryIndexer(RepositoryManager repositoryManager, IndexQueue indexQueue) {
    this.repositoryManager = repositoryManager;
    this.indexQueue = indexQueue;
  }

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public Class<Repository> getType() {
    return Repository.class;
  }

  @Override
  public String getIndex() {
    return INDEX;
  }

  @Subscribe(async = false)
  public void handleEvent(RepositoryEvent event) {
    HandlerEvents.handleEvent(this, event);
  }

  @Override
  public Updater<Repository> open() {
    return new RepositoryIndexUpdater(repositoryManager, indexQueue.getQueuedIndex(INDEX));
  }

  public static class RepositoryIndexUpdater implements Updater<Repository> {

    private final RepositoryManager repositoryManager;
    private final Index index;

    public RepositoryIndexUpdater(RepositoryManager repositoryManager, Index index) {
      this.repositoryManager = repositoryManager;
      this.index = index;
    }

    @Override
    public void store(Repository repository) {
      index.store(Id.of(repository), RepositoryPermissions.read(repository).asShiroString(), repository);
    }

    @Override
    public void delete(Repository repository) {
      index.deleteByRepository(repository.getId());
    }

    @Override
    public void reIndexAll() {
      // v1 used the whole classname as type
      index.deleteByTypeName(Repository.class.getName());
      index.deleteByType(Repository.class);
      for (Repository repository : repositoryManager.getAll()) {
        store(repository);
      }
    }

    @Override
    public void close() {
      index.close();
    }
  }
}
