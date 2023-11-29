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

package sonia.scm.importexport;

import com.google.common.io.Files;
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
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.util.IOUtil;

import java.io.File;
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
  private final WorkdirProvider workdirProvider;
  private final RepositoryImportLoggerFactory loggerFactory;

  @Inject
  public FromBundleImporter(RepositoryManager manager, RepositoryServiceFactory serviceFactory, ScmEventBus eventBus, WorkdirProvider workdirProvider, RepositoryImportLoggerFactory loggerFactory) {
    this.manager = manager;
    this.serviceFactory = serviceFactory;
    this.eventBus = eventBus;
    this.workdirProvider = workdirProvider;
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
      File workdir = workdirProvider.createNewWorkdir(repository.getId());
      try (RepositoryService service = serviceFactory.create(repository)) {
        logger.step("writing temporary dump file");
        File file = File.createTempFile("scm-import-", ".bundle", workdir);
        long length = Files.asByteSink(file).writeFrom(inputStream);
        LOG.info("copied {} bytes to temp, start bundle import", length);
        logger.step("importing repository data from dump file");
        runUnbundleCommand(compressed, service, file);
        logger.finished();
      } catch (IOException e) {
        logger.failed(e);
        throw new InternalRepositoryException(repository, "Failed to import from bundle", e);
      } finally {
        try {
          IOUtil.delete(workdir);
        } catch (IOException ex) {
          LOG.warn("could not delete temporary file", ex);
        }
      }
    };
  }

  private void runUnbundleCommand(boolean compressed, RepositoryService service, File file) throws IOException {
    AtomicReference<RepositoryHookEvent> eventSink = new AtomicReference<>();
    service.getUnbundleCommand()
      .setCompressed(compressed)
      .setPostEventSink(eventSink::set)
      .unbundle(file);
    RepositoryHookEvent repositoryHookEvent = eventSink.get();
    if (repositoryHookEvent != null) {
      eventBus.post(new PostReceiveRepositoryHookEvent(repositoryHookEvent));
      eventBus.post(new ImportRepositoryHookEvent(repositoryHookEvent));
    }
  }
}
