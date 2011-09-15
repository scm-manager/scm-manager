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

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.List;

/**
 * Class description
 *
 *
 * @version        Enter version here..., 11/09/14
 * @author         Enter your name here...
 */
public class SvnBlameViewer implements BlameViewer
{

  /** the logger for SvnChangesetViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnChangesetViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   */
  public SvnBlameViewer(SvnRepositoryHandler handler, Repository repository)
  {
    this.handler = handler;
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   *
   * @return
   */
  @Override
  public BlamePagingResult getBlame(String revision, String path)
  {
    List<BlameLine> blameLines = new ArrayList<BlameLine>();
    File directory = handler.getDirectory(repository);
    SVNRepository svnRepository = null;
    SVNURL svnurl = null;
    SVNRevision endRevision = null;

    if (Util.isNotEmpty(revision))
    {
      endRevision = SVNRevision.create(Long.parseLong(revision));
    }
    else
    {
      endRevision = SVNRevision.HEAD;
    }

    try
    {
      svnurl = SVNURL.fromFile(new File(directory, path));
      svnRepository = SVNRepositoryFactory.create(SVNURL.fromFile(directory));

      ISVNAuthenticationManager svnManager =
        svnRepository.getAuthenticationManager();
      SVNLogClient svnLogClient = new SVNLogClient(svnManager, null);

      svnLogClient.doAnnotate(svnurl, SVNRevision.UNDEFINED,
                              SVNRevision.create(1l), endRevision,
                              new SvnBlameHandler(blameLines));
    }
    catch (Exception ex)
    {
      logger.error("could not create blame view", ex);
    }

    return new BlamePagingResult(blameLines.size(), blameLines);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SvnRepositoryHandler handler;

  /** Field description */
  private Repository repository;
}
