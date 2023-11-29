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
