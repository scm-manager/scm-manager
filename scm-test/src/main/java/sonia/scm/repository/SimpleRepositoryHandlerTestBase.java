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


import org.junit.Test;
import org.mockito.stubbing.Answer;
import sonia.scm.AbstractTestBase;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class SimpleRepositoryHandlerTestBase extends AbstractTestBase {

  protected Path repoPath;
  protected Repository repository;

  protected abstract void checkDirectory(File directory);

  protected abstract RepositoryHandler createRepositoryHandler(
    ConfigurationStoreFactory factory, RepositoryLocationResolver locationResolver, File directory) throws IOException, RepositoryPathNotFoundException;

  @Test
  public void testCreate() {
    createRepository();
  }

  @Override
  protected void postSetUp() throws IOException, RepositoryPathNotFoundException {
    InMemoryConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();
    baseDirectory = new File(contextProvider.getBaseDirectory(), "repositories");
    IOUtil.mkdirs(baseDirectory);

    locationResolver = mock(RepositoryLocationResolver.class);

    RepositoryLocationResolver.RepositoryLocationResolverInstance instanceMock = mock(RepositoryLocationResolver.RepositoryLocationResolverInstance.class);
    when(locationResolver.create(any())).thenReturn(instanceMock);
    when(locationResolver.supportsLocationType(any())).thenReturn(true);
    Answer<Object> pathAnswer = ic -> {
      String id = ic.getArgument(0);
      return baseDirectory.toPath().resolve(id);
    };
    when(instanceMock.getLocation(anyString())).then(pathAnswer);
    when(instanceMock.createLocation(anyString())).then(pathAnswer);

    handler = createRepositoryHandler(storeFactory, locationResolver, baseDirectory);
  }

  @Override
  protected void preTearDown() throws Exception {
    if (handler != null) {
      handler.close();
    }
  }

  private void createRepository() {
    File nativeRepoDirectory = initRepository();

    handler.create(repository);

    assertTrue(nativeRepoDirectory.exists());
    assertTrue(nativeRepoDirectory.isDirectory());
    checkDirectory(nativeRepoDirectory);
  }

  protected File initRepository() {
    repository = RepositoryTestData.createHeartOfGold();
    File repoDirectory = new File(baseDirectory, repository.getId());
    repoPath = repoDirectory.toPath();
//    when(repoDao.getPath(repository.getId())).thenReturn(repoPath);
    return new File(repoDirectory, RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY);
  }

  protected File baseDirectory;
  protected RepositoryLocationResolver locationResolver;

  private RepositoryHandler handler;
}
