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
import sonia.scm.HandlerEventType;
import sonia.scm.plugin.Extension;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexLogStore;
import sonia.scm.search.Indexer;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SerializableIndexTask;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Extension
public class RepositoryIndexer implements Indexer<Repository> {

  @VisibleForTesting
  static final int VERSION = 3;

  private final SearchEngine searchEngine;

  @Inject
  public RepositoryIndexer(SearchEngine searchEngine) {
    this.searchEngine = searchEngine;
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
  public Class<? extends ReIndexAllTask<Repository>> getReIndexAllTask() {
    return ReIndexAll.class;
  }

  @Subscribe(async = false)
  public void handleEvent(RepositoryEvent event) {
    HandlerEventType type = event.getEventType();
    if (type.isPost()) {
      Repository repository = event.getItem();
      if (type == HandlerEventType.DELETE) {
        searchEngine.forIndices()
          .forResource(repository)
          .batch(createDeleteTask(repository));
      } else {
        searchEngine.forType(Repository.class)
          .update(createStoreTask(repository));
      }
    }
  }

  @Override
  public SerializableIndexTask<Repository> createStoreTask(Repository repository) {
    return index -> store(index, repository);
  }

  @Override
  public SerializableIndexTask<Repository> createDeleteTask(Repository repository) {
    return index -> {
      if (Repository.class.equals(index.getDetails().getType())) {
        index.delete().byId(Id.of(Repository.class, repository.getId()));
      } else {
        index.delete().byRepository(repository);
      }
    };
  }

  private static void store(Index<Repository> index, Repository repository) {
    index.store(Id.of(Repository.class, repository), RepositoryPermissions.read(repository).asShiroString(), repository);
  }

  public static class ReIndexAll extends ReIndexAllTask<Repository> {

    private final RepositoryManager repositoryManager;

    @Inject
    public ReIndexAll(IndexLogStore logStore, RepositoryManager repositoryManager) {
      super(logStore, Repository.class, VERSION);
      this.repositoryManager = repositoryManager;
    }

    @Override
    public void update(Index<Repository> index) {
      index.delete().all();
      for (Repository repository : repositoryManager.getAll()) {
        store(index, repository);
      }
    }
  }

}
