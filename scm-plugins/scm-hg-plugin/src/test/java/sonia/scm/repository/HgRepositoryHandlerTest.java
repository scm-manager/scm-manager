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

import sonia.scm.io.DefaultFileSystem;
import sonia.scm.store.StoreFactory;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgRepositoryHandlerTest extends SimpleRepositoryHandlerTestBase
{

  /**
   * Method description
   *
   *
   * @param directory
   */
  @Override
  protected void checkDirectory(File directory)
  {
    File hgDirectory = new File(directory, ".hg");

    assertTrue(hgDirectory.exists());
    assertTrue(hgDirectory.isDirectory());

    File hgrc = new File(hgDirectory, "hgrc");

    assertTrue(hgrc.exists());
    assertTrue(hgrc.isFile());
    assertTrue(hgrc.length() > 0);
  }

  /**
   * Method description
   *
   *
   * @param factory
   * @param directory
   *
   * @return
   */
  @Override
  protected RepositoryHandler createRepositoryHandler(StoreFactory factory,
          File directory)
  {
    HgRepositoryHandler handler = new HgRepositoryHandler(factory,
                                    new DefaultFileSystem());

    handler.init(contextProvider);
    handler.getConfig().setRepositoryDirectory(directory);

    // skip tests if hg not in path
    if (! handler.isConfigured())
    {
      System.out.println("WARNING could not find hg, skipping test");
      assumeTrue(false);
    }

    return handler;
  }
}
