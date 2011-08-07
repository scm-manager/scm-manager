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



package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import sonia.scm.repository.Repository;
import sonia.scm.repository.client.RepositoryClient;
import sonia.scm.repository.client.RepositoryClientException;
import sonia.scm.user.User;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class RepositoryExtendedITCase extends RepositoryITCaseBase
{

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param owner
   * @param write
   * @param read
   * @param noperm
   * @param password
   */
  public RepositoryExtendedITCase(Repository repository, User owner,
                                  User write, User read, User noperm,
                                  String password)
  {
    super(repository, owner, write, read, noperm, password);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @After
  public void cleanupTest() throws IOException
  {
    IOUtil.delete(directory);
  }

  /**
   * Method description
   *
   *
   * @throws RepositoryClientException
   */
  @Test(expected = RepositoryClientException.class)
  public void readFailed() throws RepositoryClientException
  {
    RepositoryClient rc = createRepositoryClient(nopermUser, directory);

    rc.checkout();

    // ugly workaround
    if (repository.getType().equals("git"))
    {
      for (File f : directory.listFiles())
      {
        if (!".git".equals(f.getName()))
        {
          fail("checkout works");
        }
      }

      throw new RepositoryClientException("checkout failed");
    }
  }

  /**
   * Method description
   *
   */
  @Before
  public void setupTest()
  {
    directory = createTempDirectory();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test
  public void simpleRead() throws RepositoryClientException, IOException
  {
    RepositoryClient rc = createRepositoryClient(readUser, directory);

    rc.checkout();
  }

  /**
   * Method description
   *
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test
  public void simpleWrite() throws RepositoryClientException, IOException
  {
    RepositoryClient rc = createRepositoryClient(writeUser, directory);

    rc.checkout();
    addTestFiles(rc);
  }

  /**
   * Method description
   *
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test(expected = RepositoryClientException.class)
  public void writeFailed() throws RepositoryClientException, IOException
  {
    RepositoryClient rc = createRepositoryClient(readUser, directory);

    rc.checkout();
    addTestFiles(rc);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File directory;
}
