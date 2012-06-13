/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Assume;
import org.junit.Before;

import sonia.scm.SCMContextProvider;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.HgContextProvider;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.store.MemoryStoreFactory;
import sonia.scm.util.MockUtil;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class AbstractHgCommandTestBase extends ZippedRepositoryTestBase
{

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Before
  public void initHgHandler() throws IOException
  {
    File folder = tempFolder.newFolder();
    FileSystem fileSystem = mock(FileSystem.class);

    this.handler = new HgRepositoryHandler(new MemoryStoreFactory(),
            fileSystem, new HgContextProvider());

    SCMContextProvider context = MockUtil.getSCMContextProvider(folder);

    this.handler.init(context);

    // skip tests if hg not in path
    if (!handler.isConfigured())
    {
      System.out.println("WARNING could not find hg, skipping test");
      Assume.assumeTrue(false);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getType()
  {
    return "hg";
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getZippedRepositoryResource()
  {
    return "sonia/scm/repository/spi/scm-hg-spi-test.zip";
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected HgRepositoryHandler handler;
}
