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

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.AlreadyExistsException;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryImportEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullCommandBuilder;
import sonia.scm.repository.api.PullResponse;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static sonia.scm.ContextEntry.ContextBuilder.noContext;
import static sonia.scm.importexport.RepositoryImportLogger.ImportType.URL;
import static sonia.scm.importexport.RepositoryTypeSupportChecker.checkSupport;
import static sonia.scm.importexport.RepositoryTypeSupportChecker.type;

public class FromUrlImporter {

  private static final Logger LOG = LoggerFactory.getLogger(FromUrlImporter.class);

  private final RepositoryManager manager;
  private final RepositoryServiceFactory serviceFactory;
  private final ScmEventBus eventBus;
  private final RepositoryImportLoggerFactory loggerFactory;
  private final ImportNotificationHandler notificationHandler;

  @Inject
  public FromUrlImporter(RepositoryManager manager, RepositoryServiceFactory serviceFactory, ScmEventBus eventBus, RepositoryImportLoggerFactory loggerFactory, ImportNotificationHandler notificationHandler) {
    this.manager = manager;
    this.serviceFactory = serviceFactory;
    this.eventBus = eventBus;
    this.loggerFactory = loggerFactory;
    this.notificationHandler = notificationHandler;
  }

  public Repository importFromUrl(RepositoryImportParameters parameters, Repository repository) {
    RepositoryType t = type(manager, repository.getType());
    RepositoryPermissions.create().check();
    checkSupport(t, Command.PULL);

    LOG.info("start {} import for external url {}", repository.getType(), parameters.getImportUrl());

    repository.setPermissions(singletonList(new RepositoryPermission(SecurityUtils.getSubject().getPrincipal().toString(), "OWNER", false)));

    RepositoryImportLogger logger = loggerFactory.createLogger();
    Repository createdRepository;
    try {
      createdRepository = manager.create(
        repository,
        pullChangesFromRemoteUrl(parameters, logger)
      );
    } catch (AlreadyExistsException e) {
      throw e;
    } catch (Exception e) {
      if (logger.started()) {
        logger.failed(e);
      }
      eventBus.post(new RepositoryImportEvent(repository, true));
      throw new ImportFailedException(noContext(), "Could not import repository from url " + parameters.getImportUrl(), e);
    }
    eventBus.post(new RepositoryImportEvent(createdRepository, false));
    return createdRepository;
  }

  private Consumer<Repository> pullChangesFromRemoteUrl(RepositoryImportParameters parameters, RepositoryImportLogger logger) {
    return repository -> {
      logger.start(URL, repository);
      try (RepositoryService service = serviceFactory.create(repository)) {
        PullCommandBuilder pullCommand = service.getPullCommand();
        if (!Strings.isNullOrEmpty(parameters.getUsername()) && !Strings.isNullOrEmpty(parameters.getPassword())) {
          logger.step("setting username and password for pull");
          pullCommand
            .withUsername(parameters.getUsername())
            .withPassword(parameters.getPassword());
        }
        pullCommand.doFetchLfs(!parameters.isSkipLfs());

        logger.step("pulling repository from " + parameters.getImportUrl());
        PullResponse pullResponse = pullCommand.pull(parameters.getImportUrl());
        logger.finished();
        handle(pullResponse, repository);
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "Failed to import from remote url: " + e.getMessage(), e);
      } catch (ImportFailedException e) {
        notificationHandler.handleFailedImport();
        throw e;
      }
    };
  }

  private void handle(PullResponse pullResponse, Repository repository) {
    if (pullResponse.getLfsCount().getFailureCount() == 0) {
      notificationHandler.handleSuccessfulImport(repository, pullResponse.getLfsCount());
    } else {
      notificationHandler.handleSuccessfulImportWithLfsFailures(repository, pullResponse.getLfsCount());
    }
  }

  @Getter
  @Setter
  public static class RepositoryImportParameters {
    private String importUrl;
    private String username;
    private String password;
    private boolean skipLfs;
  }
}
