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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.UnbundleResponse;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
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
   *
   * @param context
   * @param repository
   */
  public SvnUnbundleCommand(SvnContext context, Repository repository)
  {
    super(context, repository);
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
    File archive = checkNotNull(request.getArchive(), "archive is required");

    logger.debug("archive repository {} to {}", context.getDirectory(),
      archive);

    SVNClientManager clientManager = null;

    try
    {
      clientManager = SVNClientManager.newInstance();

      SVNAdminClient adminClient = clientManager.getAdminClient();

      restore(adminClient, archive, context.getDirectory());
    }
    catch (SVNException ex)
    {
      throw new IOException("could not restore dump", ex);
    }
    finally
    {
      SvnUtil.dispose(clientManager);
    }

    return new UnbundleResponse();
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
  private void restore(SVNAdminClient adminClient, File dump, File repository)
    throws SVNException, IOException
  {
    InputStream inputStream = null;

    try
    {
      inputStream = new FileInputStream(dump);
      adminClient.doLoad(repository, inputStream);
    }
    finally
    {
      Closeables.close(inputStream, true);
    }
  }
}
