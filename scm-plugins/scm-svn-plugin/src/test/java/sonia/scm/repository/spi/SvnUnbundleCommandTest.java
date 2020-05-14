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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Files;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.UnbundleResponse;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnUnbundleCommandTest extends AbstractSvnCommandTestBase
{

  @Test
  public void testUnbundle()
    throws IOException, SVNException
  {
    File bundle = bundle();
    SvnContext ctx = createEmptyContext();
    //J-
    UnbundleResponse res = new SvnUnbundleCommand(ctx)
      .unbundle(new UnbundleCommandRequest(Files.asByteSource(bundle))
    );
    //J+

    assertThat(res, notNullValue());
    assertThat(res.getChangesetCount(), is(5l));

    SVNRepository repo = ctx.open();

    assertThat(repo.getLatestRevision(), is(5l));
    SvnUtil.closeSession(repo);
  }

  private File bundle() throws IOException
  {
    File file = tempFolder.newFile();

    //J-
    new SvnBundleCommand(createContext())
      .bundle(new BundleCommandRequest(Files.asByteSink(file))
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

    return new SvnContext(repository, folder);
  }
}
