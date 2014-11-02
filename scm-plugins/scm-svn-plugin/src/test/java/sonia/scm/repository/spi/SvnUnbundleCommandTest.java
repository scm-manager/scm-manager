/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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

import com.google.common.io.Files;

import org.junit.Test;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.UnbundleResponse;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnUnbundleCommandTest extends AbstractSvnCommandTestBase
{

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   * @throws SVNException
   */
  @Test
  public void testUnbundle()
    throws IOException, RepositoryException, SVNException
  {
    File bundle = bundle();
    SvnContext ctx = createEmptyContext();
    //J-
    UnbundleResponse res = new SvnUnbundleCommand(
      ctx, 
      repository
    ).unbundle(
      new UnbundleCommandRequest(
        Files.asByteSource(bundle)
      )
    );
    //J+

    assertThat(res, notNullValue());
    assertThat(res.getChangesetCount(), is(5l));

    SVNRepository repo = ctx.open();

    assertThat(repo.getLatestRevision(), is(5l));
    SvnUtil.closeSession(repo);
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
  private File bundle() throws IOException, RepositoryException
  {
    File file = tempFolder.newFile();

    //J-
    new SvnBundleCommand(
      createContext(),
      repository
    ).bundle(
      new BundleCommandRequest(
        Files.asByteSink(file)
      )
    );
    //J+

    return file;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   * @throws SVNException
   */
  private SvnContext createEmptyContext() throws IOException, SVNException
  {
    File folder = tempFolder.newFolder();

    SVNRepositoryFactory.createLocalRepository(folder, true, true);

    return new SvnContext(folder);
  }
}
