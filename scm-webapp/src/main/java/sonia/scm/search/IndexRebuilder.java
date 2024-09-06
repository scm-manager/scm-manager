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

package sonia.scm.search;

import jakarta.inject.Inject;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.RepositoryManager;

import java.util.Set;

public class IndexRebuilder {

  private final SearchEngine searchEngine;
  private final Set<Indexer> indexers;
  private final RepositoryManager repositoryManager;

  @Inject
  public IndexRebuilder(SearchEngine searchEngine, Set<Indexer> indexers, RepositoryManager repositoryManager) {
    this.searchEngine = searchEngine;
    this.indexers = indexers;
    this.repositoryManager = repositoryManager;
  }

  public void rebuildAll() {
    for (Indexer indexer : indexers) {
      searchEngine.forType(indexer.getType()).update(indexer.getReIndexAllTask());
      repositoryManager.getAll().forEach(
        repository -> ScmEventBus.getInstance().post(new ReindexRepositoryEvent(repository))
      );
    }
  }
}
