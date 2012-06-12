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

import com.google.common.collect.Lists;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.SvnBlameHandler;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnBlameCommand extends AbstractSvnCommand implements BlameCommand
{

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param repositoryDirectory
   */
  public SvnBlameCommand(Repository repository, File repositoryDirectory)
  {
    super(repository, repositoryDirectory);
  }

  //~--- get methods ----------------------------------------------------------

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
  public BlameResult getBlameResult(BlameCommandRequest request)
          throws IOException, RepositoryException
  {
    String path = request.getPath();
    String revision = request.getRevision();
    List<BlameLine> blameLines = Lists.newArrayList();
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
      svnurl = SVNURL.fromFile(new File(repositoryDirectory, path));
      svnRepository =
        SVNRepositoryFactory.create(SVNURL.fromFile(repositoryDirectory));

      ISVNAuthenticationManager svnManager =
        svnRepository.getAuthenticationManager();
      SVNLogClient svnLogClient = new SVNLogClient(svnManager, null);

      svnLogClient.doAnnotate(svnurl, SVNRevision.UNDEFINED,
                              SVNRevision.create(1l), endRevision,
                              new SvnBlameHandler(svnRepository, path,
                                blameLines));
    }
    catch (SVNException ex)
    {
      throw new RepositoryException("could not create blame result", ex);
    }
    finally
    {
      SvnUtil.closeSession(svnRepository);
    }

    return new BlameResult(blameLines.size(), blameLines);
  }
}
