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

import com.google.common.io.ByteSink;
import com.google.common.io.Closeables;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.BundleResponse;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 */
public class SvnBundleCommand extends AbstractSvnCommand
  implements BundleCommand
{

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param repository
   */
  public SvnBundleCommand(SvnContext context, Repository repository)
  {
    super(context, repository);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param adminClient
   * @param repository
   * @param target
   *
   * @throws IOException
   * @throws SVNException
   */
  private static void dump(SVNAdminClient adminClient, File repository,
    ByteSink target)
    throws SVNException, IOException
  {
    OutputStream outputStream = null;

    try
    {
      outputStream = target.openBufferedStream();
      adminClient.doDump(repository, outputStream, SVNRevision.create(-1L),
        SVNRevision.HEAD, false, false);
    }
    finally
    {
      Closeables.close(outputStream, true);
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public BundleResponse bundle(BundleCommandRequest request)
    throws IOException, RepositoryException
  {
    ByteSink archive = checkNotNull(request.getArchive(),
                         "archive is required");

    BundleResponse response;

    SVNClientManager clientManager = null;

    try
    {
      clientManager = SVNClientManager.newInstance();

      SVNAdminClient adminClient = clientManager.getAdminClient();

      dump(adminClient, context.getDirectory(), archive);
      response = new BundleResponse(context.open().getLatestRevision());
    }
    catch (SVNException ex)
    {
      throw new IOException("could not create dump", ex);
    }
    finally
    {
      SvnUtil.dispose(clientManager);
    }

    return response;
  }
}
