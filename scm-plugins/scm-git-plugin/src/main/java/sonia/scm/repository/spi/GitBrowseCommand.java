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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.GitSubModuleParser;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SubRepository;
import sonia.scm.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

//~--- JDK imports ------------------------------------------------------------

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
   * @param context
   * @param repository
   */
  public GitBrowseCommand(GitContext context, Repository repository)
  {
    super(context, repository);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  @SuppressWarnings("unchecked")
  public BrowserResult getBrowserResult(BrowseCommandRequest request)
    throws IOException {
    logger.debug("try to create browse result for {}", request);

    BrowserResult result;

    org.eclipse.jgit.lib.Repository repo = open();
    ObjectId revId;

    if (Util.isEmpty(request.getRevision()))
    {
      revId = getDefaultBranch(repo);
    }
    else
    {
      revId = GitUtil.getRevisionId(repo, request.getRevision());
    }

    if (revId != null)
    {
      result = new BrowserResult(revId.getName(), getEntry(repo, request, revId));
    }
    else
    {
      if (Util.isNotEmpty(request.getRevision()))
      {
        logger.error("could not find revision {}", request.getRevision());
        throw notFound(entity("Revision", request.getRevision()).in(this.repository));
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find head of repository, empty?");
      }

      result = new BrowserResult(Constants.HEAD, createEmtpyRoot());
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  private FileObject createEmtpyRoot() {
    FileObject fileObject = new FileObject();
    fileObject.setName("");
    fileObject.setPath("");
    fileObject.setDirectory(true);
    return fileObject;
  }

  /**
   * Method description
   *
   * @param repo
   * @param request
   * @param revId
   * @param treeWalk
   *
   * @return
   *
   * @throws IOException
   */
  private FileObject createFileObject(org.eclipse.jgit.lib.Repository repo,
    BrowseCommandRequest request, ObjectId revId, TreeWalk treeWalk)
    throws IOException {

    FileObject file = new FileObject();

    String path = treeWalk.getPathString();

    file.setName(treeWalk.getNameString());
    file.setPath(path);

    SubRepository sub = null;

    if (!request.isDisableSubRepositoryDetection())
    {
      sub = getSubRepository(repo, revId, path);
    }

    if (sub != null)
    {
      logger.trace("{} seems to be a sub repository", path);
      file.setDirectory(true);
      file.setSubRepository(sub);
    }
    else
    {
      ObjectLoader loader = repo.open(treeWalk.getObjectId(0));

      file.setDirectory(loader.getType() == Constants.OBJ_TREE);
      file.setLength(loader.getSize());

      // don't show message and date for directories to improve performance
      if (!file.isDirectory() &&!request.isDisableLastCommit())
      {
        logger.trace("fetch last commit for {} at {}", path, revId.getName());
        RevCommit commit = getLatestCommit(repo, revId, path);

        if (commit != null)
        {
          file.setLastModified(GitUtil.getCommitTime(commit));
          file.setDescription(commit.getShortMessage());
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("could not find latest commit for {} on {}", path,
            revId);
        }
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
    catch (IOException ex)
    {
      logger.error("could not parse commit for file", ex);
    }
    finally
    {
      GitUtil.release(walk);
    }

    return result;
  }

  private FileObject getEntry(org.eclipse.jgit.lib.Repository repo, BrowseCommandRequest request, ObjectId revId) throws IOException {
    RevWalk revWalk = null;
    TreeWalk treeWalk = null;

    FileObject result;

    try {
      logger.debug("load repository browser for revision {}", revId.name());

      treeWalk = new TreeWalk(repo);
      if (!isRootRequest(request)) {
        treeWalk.setFilter(PathFilter.create(request.getPath()));
      }
      revWalk = new RevWalk(repo);

      RevTree tree = revWalk.parseTree(revId);

      if (tree != null)
      {
        treeWalk.addTree(tree);
      }
      else
      {
        throw new IllegalStateException("could not find tree for " + revId.name());
      }

      if (isRootRequest(request)) {
        result = createEmtpyRoot();
        findChildren(result, repo, request, revId, treeWalk);
      } else {
        result = findFirstMatch(repo, request, revId, treeWalk);
        if ( result.isDirectory() ) {
          treeWalk.enterSubtree();
          findChildren(result, repo, request, revId, treeWalk);
        }
      }

    }
    finally
    {
      GitUtil.release(revWalk);
      GitUtil.release(treeWalk);
    }

    return result;
  }

  private boolean isRootRequest(BrowseCommandRequest request) {
    return Strings.isNullOrEmpty(request.getPath()) || "/".equals(request.getPath());
  }

  private FileObject findChildren(FileObject parent, org.eclipse.jgit.lib.Repository repo, BrowseCommandRequest request, ObjectId revId, TreeWalk treeWalk) throws IOException {
    List<FileObject> files = Lists.newArrayList();
    while (treeWalk.next())
    {

      FileObject fileObject = createFileObject(repo, request, revId, treeWalk);
      if (!fileObject.getPath().startsWith(parent.getPath())) {
        parent.setChildren(files);
        return fileObject;
      }

      files.add(fileObject);

      if (request.isRecursive() && fileObject.isDirectory()) {
        treeWalk.enterSubtree();
        FileObject rc = findChildren(fileObject, repo, request, revId, treeWalk);
        if (rc != null) {
          files.add(rc);
        }
      }
    }

    parent.setChildren(files);

    return null;
  }

  private FileObject findFirstMatch(org.eclipse.jgit.lib.Repository repo,
                        BrowseCommandRequest request, ObjectId revId, TreeWalk treeWalk) throws IOException {
    String[] pathElements = request.getPath().split("/");
    int currentDepth = 0;
    int limit = pathElements.length;

    while (treeWalk.next()) {
      String name = treeWalk.getNameString();

      if (name.equalsIgnoreCase(pathElements[currentDepth])) {
        currentDepth++;

        if (currentDepth >= limit) {
          return createFileObject(repo, request, revId, treeWalk);
        } else {
          treeWalk.enterSubtree();
        }
      }
    }

    throw notFound(entity("File", request.getPath()).in("Revision", revId.getName()).in(this.repository));
  }

  @SuppressWarnings("unchecked")
  private Map<String,
    SubRepository> getSubRepositories(org.eclipse.jgit.lib.Repository repo,
      ObjectId revision)
    throws IOException {
    if (logger.isDebugEnabled())
    {
      logger.debug("read submodules of {} at {}", repository.getName(),
        revision);
    }

    Map<String, SubRepository> subRepositories;
    try ( ByteArrayOutputStream baos = new ByteArrayOutputStream() )
    {
      new GitCatCommand(context, repository).getContent(repo, revision,
        PATH_MODULES, baos);
      subRepositories = GitSubModuleParser.parse(baos.toString());
    }
    catch (NotFoundException ex)
    {
      logger.trace("could not find .gitmodules", ex);
      subRepositories = Collections.EMPTY_MAP;
    }

    return subRepositories;
  }

  private SubRepository getSubRepository(org.eclipse.jgit.lib.Repository repo,
    ObjectId revId, String path)
    throws IOException {
    Map<String, SubRepository> subRepositories = subrepositoryCache.get(revId);

    if (subRepositories == null)
    {
      subRepositories = getSubRepositories(repo, revId);
      subrepositoryCache.put(revId, subRepositories);
    }

    SubRepository sub = null;

    if (subRepositories != null)
    {
      sub = subRepositories.get(path);
    }

    return sub;
  }

  //~--- fields ---------------------------------------------------------------
  
  /** sub repository cache */
  private final Map<ObjectId, Map<String, SubRepository>> subrepositoryCache = Maps.newHashMap();
}
