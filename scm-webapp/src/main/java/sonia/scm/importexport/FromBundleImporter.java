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

package sonia.scm.importexport;

import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.ImportRepositoryHookEvent;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryImportEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static sonia.scm.importexport.RepositoryImportLogger.ImportType.DUMP;
import static sonia.scm.importexport.RepositoryTypeSupportChecker.checkSupport;
import static sonia.scm.importexport.RepositoryTypeSupportChecker.type;

public class FromBundleImporter {

  private static final Logger LOG = LoggerFactory.getLogger(FromBundleImporter.class);

  private final RepositoryManager manager;
  private final RepositoryServiceFactory serviceFactory;
  private final ScmEventBus eventBus;
  private final RepositoryImportLoggerFactory loggerFactory;

  @Inject
  public FromBundleImporter(RepositoryManager manager, RepositoryServiceFactory serviceFactory, ScmEventBus eventBus, RepositoryImportLoggerFactory loggerFactory) {
    this.manager = manager;
    this.serviceFactory = serviceFactory;
    this.eventBus = eventBus;
    this.loggerFactory = loggerFactory;
  }

  public Repository importFromBundle(boolean compressed, InputStream inputStream, Repository repository) {
    RepositoryPermissions.create().check();

    RepositoryType t = type(manager, repository.getType());
    checkSupport(t, Command.UNBUNDLE);

    repository.setPermissions(singletonList(
      new RepositoryPermission(SecurityUtils.getSubject().getPrincipal().toString(), "OWNER", false)
    ));

    RepositoryImportLogger logger = loggerFactory.createLogger();

    try {
      repository = manager.create(repository, unbundleImport(inputStream, compressed, logger));
    } catch (Exception e) {
      logger.failed(e);
      eventBus.post(new RepositoryImportEvent(repository, true));
      throw e;
    }

    eventBus.post(new RepositoryImportEvent(repository, false));
    return repository;
  }

  private Consumer<Repository> unbundleImport(InputStream inputStream, boolean compressed, RepositoryImportLogger logger) {
    return repository -> {
      logger.start(DUMP, repository);
      try (RepositoryService service = serviceFactory.create(repository)) {
        runUnbundleCommand(compressed, service, inputStream);
        logger.finished();
      } catch (IOException e) {
        logger.failed(e);
        throw new InternalRepositoryException(repository, "Failed to import from bundle", e);
      }
    };
  }

  private void runUnbundleCommand(boolean compressed, RepositoryService service, InputStream inputStream) throws IOException {
    AtomicReference<RepositoryHookEvent> eventSink = new AtomicReference<>();
    service.getUnbundleCommand()
      .setCompressed(compressed)
      .setPostEventSink(eventSink::set)
      .unbundle(inputStream);
    RepositoryHookEvent repositoryHookEvent = eventSink.get();
    if (repositoryHookEvent != null) {
      eventBus.post(new PostReceiveRepositoryHookEvent(repositoryHookEvent));
      eventBus.post(new ImportRepositoryHookEvent(repositoryHookEvent));
    }
  }
}
