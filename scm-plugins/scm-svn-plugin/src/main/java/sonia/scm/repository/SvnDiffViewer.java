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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.DefaultSVNDiffGenerator;
import org.tmatesoft.svn.core.wc.ISVNDiffGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnDiffViewer implements DiffViewer
{

  /** the logger for SvnDiffViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnDiffViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param directory
   */
  public SvnDiffViewer(File directory)
  {
    this.directory = directory;
  }

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   */
  public SvnDiffViewer(SvnRepositoryHandler handler, Repository repository)
  {
    this(handler.getDirectory(repository));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   * @param output
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void getDiff(String revision, String path, OutputStream output)
          throws IOException, RepositoryException
  {
    AssertUtil.assertIsNotEmpty(revision);
    AssertUtil.assertIsNotNull(output);

    try
    {
      SVNURL svnurl = SVNURL.fromFile(directory);

      if (Util.isNotEmpty(path))
      {
        svnurl = svnurl.appendPath(path, true);
      }

      SVNClientManager clientManager = SVNClientManager.newInstance();
      SVNDiffClient diffClient = clientManager.getDiffClient();
      ISVNDiffGenerator diffGenerator = diffClient.getDiffGenerator();

      if (diffGenerator == null)
      {
        diffGenerator = new DefaultSVNDiffGenerator();
      }

      diffGenerator.setDiffAdded(true);
      diffGenerator.setDiffDeleted(true);
      diffClient.setDiffGenerator(diffGenerator);

      long currentRev = Long.parseLong(revision);

      diffClient.doDiff(svnurl, SVNRevision.HEAD,
                        SVNRevision.create(currentRev),
                        SVNRevision.create(currentRev - 1), SVNDepth.INFINITY,
                        false, output);
    }
    catch (Exception ex)
    {
      logger.error("could not create blame view", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File directory;
}
