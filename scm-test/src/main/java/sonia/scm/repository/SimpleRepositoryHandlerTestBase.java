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
