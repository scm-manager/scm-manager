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

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class description
 *
 *
 * @author Sebastian Sdorra
 */
public class GitBlameViewer implements BlameViewer
{

  /** the logger for GitChangesetViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(GitChangesetViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   */
  public GitBlameViewer(GitRepositoryHandler handler, Repository repository)
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
    BlameResult blameResult = null;
    BlamePagingResult blamePagingResult = null;
    org.eclipse.jgit.lib.Repository gr = null;
    File directory = handler.getDirectory(repository);
    Git git = null;

    try
    {
      gr = GitUtil.open(directory);
      git = new Git(gr);

      BlameCommand blame = git.blame();

      if (path != null)
      {
        blame.setFilePath(path);
      }

      blameResult = blame.call();
      AssertUtil.assertIsNotNull(blameResult);

      List<BlameLine> blameLines = new ArrayList<BlameLine>();
      int total = blameResult.getResultContents().size();

      for (int i = 0; i < total; i++)
      {
        PersonIdent author = blameResult.getSourceAuthor(i);
        BlameLine blameLine = new BlameLine();

        blameLine.setLineNumber(i);
        blameLine.setAuthor(new Person(author.getName(),
                                       author.getEmailAddress()));

        RevCommit commit = blameResult.getSourceCommit(i);
        long when = GitUtil.getCommitTime(commit);

        blameLine.setWhen(when);

        String rev = commit.getId().getName();

        blameLine.setRevision(rev);

        String content = blameResult.getResultContents().getString(i);

        blameLine.setCode(content);
        blameLines.add(blameLine);
      }

      blamePagingResult = new BlamePagingResult(total, blameLines);
    }
    catch (IOException ex)
    {
      logger.error("could not open repository", ex);
    }

    return blamePagingResult;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler handler;

  /** Field description */
  private Repository repository;
}
