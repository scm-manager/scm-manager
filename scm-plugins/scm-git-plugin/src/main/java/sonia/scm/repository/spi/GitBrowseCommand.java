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

import org.eclipse.jgit.errors.MissingObjectException;
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

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.GitSubModuleParser;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.PathNotFoundException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.SubRepository;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitBrowseCommand extends AbstractGitCommand
        implements BrowseCommand
{

  /** Field description */
  public static final String PATH_MODULES = ".gitmodules";

  /**
   * the logger for GitBrowseCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitBrowseCommand.class);

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
  public GitBrowseCommand(GitContext context, Repository repository)
  {
    super(context, repository);
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
  public BrowserResult getBrowserResult(BrowseCommandRequest request)
          throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("try to create browse result for {}", request);
    }

    BrowserResult result = null;
    org.eclipse.jgit.lib.Repository repo = open();
    ObjectId revId = null;

    if (Util.isEmpty(request.getRevision()))
    {
      revId = GitUtil.getRepositoryHead(repo);
    }
    else
    {
      revId = GitUtil.getRevisionId(repo, request.getRevision());
    }

    if (revId != null)
    {
      result = getResult(repo, revId, request.getPath());
    }
    else
    {
      if (Util.isNotEmpty(request.getRevision()))
      {
        logger.error("could not find revision {}", request.getRevision());
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("coul not find head of repository, empty?");
      }

      result = new BrowserResult(Constants.HEAD, null, null,
                                 new ArrayList<FileObject>());
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param files
   * @param repo
   * @param revId
   * @param path
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private void appendSubModules(List<FileObject> files,
                                org.eclipse.jgit.lib.Repository repo,
                                ObjectId revId, String path)
          throws IOException, RepositoryException
  {
    path = Util.nonNull(path);

    Map<String, SubRepository> subRepositories = getSubRepositories(repo,
                                                   revId);

    if (subRepositories != null)
    {
      for (Entry<String, SubRepository> e : subRepositories.entrySet())
      {
        String p = e.getKey();

        if (p.startsWith(path))
        {
          p = p.substring(path.length());

          if (p.startsWith("/"))
          {
            p = p.substring(1);
          }

          if (p.endsWith("/"))
          {
            p = p.substring(0, p.length() - 1);
          }

          if (!p.contains("/"))
          {
            FileObject fo = new FileObject();

            fo.setDirectory(true);
            fo.setPath(path);
            fo.setName(p);
            fo.setSubRepository(e.getValue());
            files.add(fo);
          }
        }
      }
    }
  }

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
    FileObject file = null;

    try
    {
      file = new FileObject();

      String path = treeWalk.getPathString();

      file.setName(treeWalk.getNameString());
      file.setPath(path);

      ObjectLoader loader = repo.open(treeWalk.getObjectId(0));

      file.setDirectory(loader.getType() == Constants.OBJ_TREE);
      file.setLength(loader.getSize());

      // don't show message and date for directories to improve performance
      if (!file.isDirectory())
      {
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
      }
    }
    catch (MissingObjectException ex)
    {
      file = null;
      logger.error("could not fetch object for id {}", revId);

      if (logger.isTraceEnabled())
      {
        logger.trace("could not fetch object", ex);
      }
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

  /**
   * Method description
   *
   *
   * @param repo
   * @param revId
   * @param path
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private BrowserResult getResult(org.eclipse.jgit.lib.Repository repo,
                                  ObjectId revId, String path)
          throws IOException, RepositoryException
  {
    BrowserResult result = null;
    RevWalk revWalk = null;
    TreeWalk treeWalk = null;

    try
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("load repository browser for revision {}", revId.name());
      }

      treeWalk = new TreeWalk(repo);
      revWalk = new RevWalk(repo);

      RevTree tree = revWalk.parseTree(revId);

      if (tree != null)
      {
        treeWalk.addTree(tree);
      }
      else
      {
        logger.error("could not find tree for {}", revId.name());
      }

      result = new BrowserResult();

      List<FileObject> files = new ArrayList<FileObject>();

      appendSubModules(files, repo, revId, path);

      if (Util.isEmpty(path))
      {
        while (treeWalk.next())
        {
          FileObject fo = createFileObject(repo, revId, treeWalk);

          if (fo != null)
          {
            files.add(fo);
          }
        }
      }
      else
      {
        String[] parts = path.split("/");
        int current = 0;
        int limit = parts.length;

        while (treeWalk.next())
        {
          String name = treeWalk.getNameString();

          if (current >= limit)
          {
            String p = treeWalk.getPathString();

            if (p.split("/").length > limit)
            {
              FileObject fo = createFileObject(repo, revId, treeWalk);

              if (fo != null)
              {
                files.add(fo);
              }
            }
          }
          else if (name.equalsIgnoreCase(parts[current]))
          {
            current++;
            treeWalk.enterSubtree();
          }
        }
      }

      result.setFiles(files);
      result.setRevision(revId.getName());
    }
    finally
    {
      GitUtil.release(revWalk);
      GitUtil.release(treeWalk);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param repo
   * @param revision
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private Map<String, SubRepository> getSubRepositories(
          org.eclipse.jgit.lib.Repository repo, ObjectId revision)
          throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("read submodules of {} at {}", repository.getName(),
                   revision);
    }

    Map<String, SubRepository> subRepositories = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try
    {
      new GitCatCommand(context, repository).getContent(repo, revision,
                        PATH_MODULES, baos);
      subRepositories = GitSubModuleParser.parse(baos.toString());
    }
    catch (PathNotFoundException ex)
    {
      logger.trace("could not find .gitmodules", ex);
    }

    return subRepositories;
  }
}
