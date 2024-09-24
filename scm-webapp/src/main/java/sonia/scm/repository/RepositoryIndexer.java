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

package sonia.scm.repository;

import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.HandlerEventType;
import sonia.scm.plugin.Extension;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexLogStore;
import sonia.scm.search.Indexer;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SerializableIndexTask;

@Singleton
@Extension
public class RepositoryIndexer implements Indexer<Repository> {

  @VisibleForTesting
  static final int VERSION = 4;

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
        index.delete().byId(id(repository));
      } else {
        index.delete().by(Repository.class, repository).execute();
      }
    };
  }

  private static void store(Index<Repository> index, Repository repository) {
    index.store(
      id(repository),
      RepositoryPermissions.read(repository).asShiroString(),
      repository
    );
  }

  private static Id<Repository> id(Repository repository) {
    return Id.of(Repository.class, repository).and(repository);
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
