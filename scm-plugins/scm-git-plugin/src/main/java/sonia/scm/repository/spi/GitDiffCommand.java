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

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitDiffCommand extends AbstractGitCommand implements DiffCommand
{

  /**
   * the logger for GitDiffCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitDiffCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param repositoryDirectory
   */
  public GitDiffCommand(Repository repository, File repositoryDirectory)
  {
    super(repository, repositoryDirectory);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param output
   */
  @Override
  public void getDiffResult(DiffCommandRequest request, OutputStream output)
  {
    org.eclipse.jgit.lib.Repository gr = null;
    RevWalk walk = null;
    TreeWalk treeWalk = null;
    DiffFormatter formatter = null;

    try
    {
      gr = open();
      walk = new RevWalk(gr);

      RevCommit commit = walk.parseCommit(gr.resolve(request.getRevision()));

      walk.markStart(commit);
      commit = walk.next();
      treeWalk = new TreeWalk(gr);
      treeWalk.reset();
      treeWalk.setRecursive(true);

      if (Util.isNotEmpty(request.getPath()))
      {
        treeWalk.setFilter(PathFilter.create(request.getPath()));
      }

      if (commit.getParentCount() > 0)
      {
        RevTree tree = commit.getParent(0).getTree();

        if (tree != null)
        {
          treeWalk.addTree(tree);
        }
        else
        {
          treeWalk.addTree(new EmptyTreeIterator());
        }
      }
      else
      {
        treeWalk.addTree(new EmptyTreeIterator());
      }

      treeWalk.addTree(commit.getTree());
      formatter = new DiffFormatter(new BufferedOutputStream(output));
      formatter.setRepository(gr);

      List<DiffEntry> entries = DiffEntry.scan(treeWalk);

      for (DiffEntry e : entries)
      {
        if (!e.getOldId().equals(e.getNewId()))
        {
          formatter.format(e);
        }
      }

      formatter.flush();
    }
    catch (Exception ex)
    {
      logger.error("could not create diff", ex);
    }
    finally
    {
      GitUtil.release(walk);
      GitUtil.release(treeWalk);
      GitUtil.release(formatter);
      GitUtil.close(gr);
    }
  }
}
