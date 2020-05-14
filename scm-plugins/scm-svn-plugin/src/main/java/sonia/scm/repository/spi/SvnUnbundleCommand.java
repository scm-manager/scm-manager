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

import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.UnbundleResponse;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 */
public class SvnUnbundleCommand extends AbstractSvnCommand
  implements UnbundleCommand
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnUnbundleCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *  @param context
   *
   */
  public SvnUnbundleCommand(SvnContext context)
  {
    super(context);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public UnbundleResponse unbundle(UnbundleCommandRequest request)
    throws IOException
  {
    ByteSource archive = checkNotNull(request.getArchive(),
                           "archive is required");

    logger.debug("archive repository {} to {}", context.getDirectory(),
      archive);

    UnbundleResponse response;

    SVNClientManager clientManager = null;

    try
    {
      clientManager = SVNClientManager.newInstance();

      SVNAdminClient adminClient = clientManager.getAdminClient();

      restore(adminClient, archive, context.getDirectory());

      response = new UnbundleResponse(context.open().getLatestRevision());
    }
    catch (SVNException ex)
    {
      throw new IOException("could not restore dump", ex);
    }
    finally
    {
      SvnUtil.dispose(clientManager);
    }

    return response;
  }

  /**
   * Method description
   *
   *
   * @param adminClient
   * @param dump
   * @param repository
   *
   * @throws IOException
   * @throws SVNException
   */
  private void restore(SVNAdminClient adminClient, ByteSource dump,
    File repository)
    throws SVNException, IOException
  {
    InputStream inputStream = null;

    try
    {
      inputStream = dump.openBufferedStream();
      adminClient.doLoad(repository, inputStream);
    }
    finally
    {
      Closeables.close(inputStream, true);
    }
  }
}
