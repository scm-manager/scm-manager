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
import jakarta.inject.Singleton;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import java.util.Optional;
import java.util.Set;

@Singleton
@Extension
@SuppressWarnings("rawtypes")
public class IndexBootstrapListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(IndexBootstrapListener.class);

  private final SearchEngine searchEngine;
  private final IndexLogStore indexLogStore;
  private final Set<Indexer> indexers;

  @Inject
  public IndexBootstrapListener(SearchEngine searchEngine, IndexLogStore indexLogStore, Set<Indexer> indexers) {
    this.searchEngine = searchEngine;
    this.indexLogStore = indexLogStore;
    this.indexers = indexers;
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    for (Indexer indexer : indexers) {
      bootstrap(indexer);
    }
  }

  private void bootstrap(Indexer indexer) {
    Optional<IndexLog> indexLog = indexLogStore.defaultIndex().get(indexer.getType());
    if (indexLog.isPresent()) {
      int version = indexLog.get().getVersion();
      if (version != indexer.getVersion()) {
        LOG.debug(
          "index version {} is older then {}, start reindexing of all {}",
          version, indexer.getVersion(), indexer.getType()
        );
        indexAll(indexer);
      }
    } else {
      LOG.debug("could not find log entry for {} index, start reindexing", indexer.getType());
      indexAll(indexer);
    }
  }

  @SuppressWarnings("unchecked")
  private void indexAll(Indexer indexer) {
    searchEngine.forType(indexer.getType()).update(indexer.getReIndexAllTask());
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    // nothing to destroy here
  }
}
