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

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryBrowser implements RepositoryBrowser
{

  /** the logger for GitRepositoryBrowser */
  private static final Logger logger =
    LoggerFactory.getLogger(GitRepositoryBrowser.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   */
  public GitRepositoryBrowser(GitRepositoryHandler handler,
                              Repository repository)
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
   * @param output
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void getContent(String revision, String path, OutputStream output)
          throws IOException, RepositoryException
  {
    File directory = handler.getDirectory(repository);
    org.eclipse.jgit.lib.Repository repo = GitUtil.open(directory);
    TreeWalk treeWalk = null;
    RevWalk revWalk = null;

    try
    {
      treeWalk = new TreeWalk(repo);

      ObjectId revId = GitUtil.getRevisionId(repo, revision);

      revWalk = new RevWalk(repo);

      RevCommit entry = revWalk.parseCommit(revId);
      RevTree revTree = entry.getTree();

      treeWalk.addTree(revTree);
      treeWalk.setFilter(PathFilter.create(path));

      if (treeWalk.next())
      {

        // Path exists
        if (treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB)
        {
          ObjectId blobId = treeWalk.getObjectId(0);
          ObjectLoader loader = repo.open(blobId);

          loader.copyTo(output);
        }
        else
        {

          // Not a blob, its something else (tree, gitlink)
          throw new PathNotFoundException(path);
        }
      }
      else
      {
        throw new PathNotFoundException(path);
      }
    }
    finally
    {
      GitUtil.release(revWalk);
      GitUtil.release(treeWalk);
      GitUtil.close(repo);
    }
  }

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public BrowserResult getResult(String revision, String path)
          throws IOException, RepositoryException
  {
    BrowserResult result = null;
    File directory = handler.getDirectory(repository);
    org.eclipse.jgit.lib.Repository repo = GitUtil.open(directory);
    RevWalk revWalk = null;
    TreeWalk treeWalk = null;

    try
    {
      ObjectId revId = GitUtil.getRevisionId(repo, revision);

      treeWalk = new TreeWalk(repo);
      revWalk = new RevWalk(repo);
      treeWalk.addTree(revWalk.parseTree(revId));
      result = new BrowserResult();

      List<FileObject> files = new ArrayList<FileObject>();

      while (treeWalk.next())
      {
        files.add(createFileObject(repo, revId, treeWalk));
      }

      result.setFiles(files);
      result.setRevision(revId.getName());
    }
    finally
    {
      GitUtil.close(repo);
      GitUtil.release(revWalk);
      GitUtil.release(treeWalk);
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   *
   * @param repo
   * @param revId
   * @param treeWalk
   *
   * @return
   *
   * @throws IOException
   */
  private FileObject createFileObject(org.eclipse.jgit.lib.Repository repo,
          ObjectId revId, TreeWalk treeWalk)
          throws IOException
  {
    FileObject file = new FileObject();
    String path = treeWalk.getPathString();

    file.setName(treeWalk.getNameString());
    file.setPath(path);

    ObjectLoader loader = repo.open(treeWalk.getObjectId(0));

    file.setDirectory(loader.getType() == Constants.OBJ_TREE);
    file.setLength(loader.getSize());

    RevCommit commit = getLatestCommit(repo, revId, path);

    if (commit != null)
    {
      file.setLastModified(GitUtil.getCommitTime(commit));
      file.setDescription(commit.getShortMessage());
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find latest commit for {} on {}", path, revId);
    }

    return file;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param repo
   * @param revId
   * @param path
   *
   * @return
   */
  private RevCommit getLatestCommit(org.eclipse.jgit.lib.Repository repo,
                                    ObjectId revId, String path)
  {
    RevCommit result = null;
    RevWalk walk = null;

    try
    {
      walk = new RevWalk(repo);
      walk.setTreeFilter(AndTreeFilter.create(PathFilter.create(path),
              TreeFilter.ANY_DIFF));

      RevCommit commit = walk.parseCommit(revId);

      walk.markStart(commit);
      result = Util.getFirst(walk);
    }
    catch (Exception ex)
    {
      logger.error("could not parse commit for file", ex);
    }
    finally
    {
      GitUtil.release(walk);
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler handler;

  /** Field description */
  private Repository repository;
}
