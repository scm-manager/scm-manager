/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;
import sonia.scm.AbstractTestBase;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
public abstract class SimpleRepositoryHandlerTestBase extends AbstractTestBase {


  protected PathBasedRepositoryDAO repoDao = mock(PathBasedRepositoryDAO.class);
  protected Path repoPath;
  protected Repository repository;

  protected abstract void checkDirectory(File directory);

  protected abstract RepositoryHandler createRepositoryHandler(
    ConfigurationStoreFactory factory, File directory) throws IOException, RepositoryPathNotFoundException;

  @Test
  public void testCreate() {
    createRepository();
  }

  @Test
  public void testCreateResourcePath() {
    createRepository();

    String path = handler.createResourcePath(repository);

    assertNotNull(path);
    assertTrue(path.trim().length() > 0);
    assertTrue(path.contains(repository.getId()));
  }

  @Override
  protected void postSetUp() throws IOException, RepositoryPathNotFoundException {
    InMemoryConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();
    baseDirectory = new File(contextProvider.getBaseDirectory(), "repositories");
    IOUtil.mkdirs(baseDirectory);
    handler = createRepositoryHandler(storeFactory, baseDirectory);
  }

  @Override
  protected void preTearDown() throws Exception {
    if (handler != null) {
      handler.close();
    }
  }

  private Repository createRepository() {
    File nativeRepoDirectory = initRepository();

    handler.create(repository);


    assertTrue(nativeRepoDirectory.exists());
    assertTrue(nativeRepoDirectory.isDirectory());
    checkDirectory(nativeRepoDirectory);

    return repository;
  }

  protected File initRepository() {
    repository = RepositoryTestData.createHeartOfGold();
    File repoDirectory = new File(baseDirectory, repository.getId());
    repoPath = repoDirectory.toPath();
    when(repoDao.getPath(repository)).thenReturn(repoPath);
    return new File(repoDirectory, RepositoryLocationResolver.REPOSITORIES_NATIVE_DIRECTORY);
  }

  protected File baseDirectory;

  private RepositoryHandler handler;
}
