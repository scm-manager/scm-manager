/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import sonia.scm.AbstractTestBase;
import sonia.scm.store.MemoryStoreFactory;
import sonia.scm.store.StoreFactory;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class SimpleRepositoryHandlerTestBase extends AbstractTestBase
{

  /**
   * Method description
   *
   *
   * @param directory
   */
  protected abstract void checkDirectory(File directory);

  /**
   * Method description
   *
   *
   * @param factory
   * @param directory
   *
   * @return
   */
  protected abstract RepositoryHandler createRepositoryHandler(
          StoreFactory factory, File directory);

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testCreate() throws RepositoryException, IOException
  {
    createRepository();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test(expected = RepositoryAllreadyExistExeption.class)
  public void testCreateExisitingRepository()
          throws RepositoryException, IOException
  {
    createRepository();
    createRepository();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testCreateResourcePath() throws RepositoryException, IOException
  {
    Repository repository = createRepository();
    String path = handler.createResourcePath(repository);

    assertNotNull(path);
    assertTrue(path.trim().length() > 0);
    assertTrue(path.contains(repository.getName()));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testDelete() throws RepositoryException, IOException
  {
    Repository repository = createRepository();

    handler.delete(repository);

    File directory = new File(baseDirectory, repository.getName());

    assertFalse(directory.exists());
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  protected void postSetUp() throws Exception
  {
    MemoryStoreFactory storeFactory = new MemoryStoreFactory();

    storeFactory.init(contextProvider);
    baseDirectory = new File(contextProvider.getBaseDirectory(),
                             "repositories");
    IOUtil.mkdirs(baseDirectory);
    handler = createRepositoryHandler(storeFactory, baseDirectory);
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  protected void preTearDown() throws Exception
  {
    if (handler != null)
    {
      handler.close();
    }
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private Repository createRepository() throws RepositoryException, IOException
  {
    Repository repository = RepositoryTestData.createHeartOfGold();

    handler.create(repository);

    File directory = new File(baseDirectory, repository.getName());

    assertTrue(directory.exists());
    assertTrue(directory.isDirectory());
    checkDirectory(directory);

    return repository;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected File baseDirectory;

  /** Field description */
  private RepositoryHandler handler;
}
