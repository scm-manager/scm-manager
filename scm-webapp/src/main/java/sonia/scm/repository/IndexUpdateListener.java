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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.plugin.Extension;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexLog;
import sonia.scm.search.IndexLogStore;
import sonia.scm.search.IndexNames;
import sonia.scm.search.IndexQueue;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Optional;

@Extension
@Singleton
public class IndexUpdateListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(IndexUpdateListener.class);

  @VisibleForTesting
  static final int INDEX_VERSION = 2;

  private final AdministrationContext administrationContext;
  private final IndexQueue queue;
  private final IndexLogStore indexLogStore;

  @Inject
  public IndexUpdateListener(AdministrationContext administrationContext, IndexQueue queue, IndexLogStore indexLogStore) {
    this.administrationContext = administrationContext;
    this.queue = queue;
    this.indexLogStore = indexLogStore;
  }

  @Subscribe(async = false)
  public void handleEvent(RepositoryEvent event) {
    HandlerEventType type = event.getEventType();
    if (type.isPost()) {
      updateIndex(type, event.getItem());
    }
  }


  private void updateIndex(HandlerEventType type, Repository repository) {
    try (Index index = queue.getQueuedIndex(IndexNames.DEFAULT)) {
      if (type == HandlerEventType.DELETE) {
        index.deleteByRepository(repository.getId());
      } else {
        store(index, repository);
      }
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    Optional<IndexLog> indexLog = indexLogStore.get(IndexNames.DEFAULT, Repository.class);
    if (indexLog.isPresent()) {
      int version = indexLog.get().getVersion();
      if (version < INDEX_VERSION) {
        LOG.debug("repository index {} is older then {}, start reindexing of all repositories", version, INDEX_VERSION);
        indexAll();
      }
    } else {
      LOG.debug("could not find log entry for repository index, start reindexing of all repositories");
      indexAll();
    }
  }

  private void indexAll() {
    administrationContext.runAsAdmin(ReIndexAll.class);
    indexLogStore.log(IndexNames.DEFAULT, Repository.class, INDEX_VERSION);
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    // we have nothing to destroy
  }

  private static void store(Index index, Repository repository) {
    index.store(Id.of(repository), RepositoryPermissions.read(repository).asShiroString(), repository);
  }

  static class ReIndexAll implements PrivilegedAction {

    private final RepositoryManager repositoryManager;
    private final IndexQueue queue;

    @Inject
    public ReIndexAll(RepositoryManager repositoryManager, IndexQueue queue) {
      this.repositoryManager = repositoryManager;
      this.queue = queue;
    }

    @Override
    public void run() {
      try (Index index = queue.getQueuedIndex(IndexNames.DEFAULT)) {
        // delete v1 types
        index.deleteByTypeName(Repository.class.getName());
        for (Repository repository : repositoryManager.getAll()) {
          store(index, repository);
        }
      }
    }

  }

}
