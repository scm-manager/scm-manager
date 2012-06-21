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

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.DefaultSVNDiffGenerator;
import org.tmatesoft.svn.core.wc.ISVNDiffGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnDiffCommand extends AbstractSvnCommand implements DiffCommand
{

  /**
   * the logger for SvnDiffCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnDiffCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param repository
   * @param repositoryDirectory
   */
  public SvnDiffCommand(SvnContext context, Repository repository)
  {
    super(context, repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param output
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void getDiffResult(DiffCommandRequest request, OutputStream output)
          throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create diff for {}", request);
    }

    Preconditions.checkNotNull(request, "request is required");
    Preconditions.checkNotNull(output, "outputstream is required");

    String path = request.getPath();
    SVNClientManager clientManager = null;

    try
    {
      SVNURL svnurl = context.createUrl();

      if (Util.isNotEmpty(path))
      {
        svnurl = svnurl.appendPath(path, true);
      }

      clientManager = SVNClientManager.newInstance();

      SVNDiffClient diffClient = clientManager.getDiffClient();
      ISVNDiffGenerator diffGenerator = diffClient.getDiffGenerator();

      if (diffGenerator == null)
      {
        diffGenerator = new DefaultSVNDiffGenerator();
      }

      diffGenerator.setDiffAdded(true);
      diffGenerator.setDiffDeleted(true);
      diffClient.setDiffGenerator(diffGenerator);

      long currentRev = SvnUtil.getRevisionNumber(request.getRevision());

      diffClient.doDiff(svnurl, SVNRevision.HEAD,
                        SVNRevision.create(currentRev - 1),
                        SVNRevision.create(currentRev), SVNDepth.INFINITY,
                        false, output);
    }
    catch (SVNException ex)
    {
      throw new RepositoryException("could not create diff", ex);
    }
    finally
    {
      SvnUtil.dispose(clientManager);
    }
  }
}
