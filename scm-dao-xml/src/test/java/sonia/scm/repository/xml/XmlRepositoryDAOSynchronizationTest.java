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

package sonia.scm.repository.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryExportingCheck;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XmlRepositoryDAOSynchronizationTest {

  private static final int CREATION_COUNT = 100;
  private static final long TIMEOUT = 10L;

  @Mock
  private SCMContextProvider provider;
  @Mock
  private RepositoryExportingCheck repositoryExportingCheck;

  private FileSystem fileSystem;
  private PathBasedRepositoryLocationResolver resolver;

  private XmlRepositoryDAO repositoryDAO;

  @BeforeEach
  void setUpObjectUnderTest(@TempDir Path path) {
    when(provider.getBaseDirectory()).thenReturn(path.toFile());

    when(provider.resolve(any())).then(ic -> {
      Path args = ic.getArgument(0);
      return path.resolve(args);
    });

    fileSystem = new DefaultFileSystem();

    resolver = new PathBasedRepositoryLocationResolver(
      provider, new InitialRepositoryLocationResolver(emptySet()), fileSystem
    );

    repositoryDAO = new XmlRepositoryDAO(resolver, fileSystem, repositoryExportingCheck);
  }

  @Test
  @Timeout(TIMEOUT)
  void shouldCreateALotOfRepositoriesInSerial() {
    for (int i=0; i<CREATION_COUNT; i++) {
      repositoryDAO.add(new Repository("repo_" + i, "git", "sync_it", "repo_" + i));
    }
    assertCreated();
  }

  private void assertCreated() {
    XmlRepositoryDAO assertionDao = new XmlRepositoryDAO(resolver, fileSystem, repositoryExportingCheck);
    assertThat(assertionDao.getAll()).hasSize(CREATION_COUNT);
  }

  @Test
  @Timeout(TIMEOUT)
  void shouldCreateALotOfRepositoriesInParallel() throws InterruptedException {
    ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    final XmlRepositoryDAO repositoryDAO = new XmlRepositoryDAO(resolver, fileSystem, repositoryExportingCheck);
    for (int i=0; i<CREATION_COUNT; i++) {
      executors.submit(create(repositoryDAO, i));
    }
    executors.shutdown();
    executors.awaitTermination(TIMEOUT, TimeUnit.SECONDS);

    assertCreated();
  }

  private Runnable create(XmlRepositoryDAO repositoryDAO, int index) {
    return () -> repositoryDAO.add(new Repository("repo_" + index, "git", "sync_it", "repo_" + index));
  }

}
