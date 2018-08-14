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

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.PathNotFoundException;
import sonia.scm.repository.RepositoryException;
import sonia.scm.util.Util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class GitCatCommand extends AbstractGitCommand implements CatCommand
{

  /**
   * the logger for GitCatCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitCatCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param repository
   */
  public GitCatCommand(GitContext context,
                       sonia.scm.repository.Repository repository)
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
  public void getCatResult(CatCommandRequest request, OutputStream output)
          throws IOException, RepositoryException
  {
    logger.debug("try to read content for {}", request);

    org.eclipse.jgit.lib.Repository repo = open();
    
    ObjectId revId = getCommitOrDefault(repo, request.getRevision());
    getContent(repo, revId, request.getPath(), output);
  }

  @Override
  public InputStream getCatResultStream(CatCommandRequest request) throws IOException, RepositoryException {
    logger.debug("try to read content for {}", request);

    org.eclipse.jgit.lib.Repository repo = open();

    ObjectId revId = getCommitOrDefault(repo, request.getRevision());
    ClosableObjectLoaderContainer closableObjectLoaderContainer = getLoader(repo, revId, request.getPath());
    return new InputStreamWrapper(closableObjectLoaderContainer);
  }

  void getContent(org.eclipse.jgit.lib.Repository repo, ObjectId revId,
    String path, OutputStream output)
    throws IOException, RepositoryException
  {
    ClosableObjectLoaderContainer closableObjectLoaderContainer = getLoader(repo, revId, path);
    try {
      closableObjectLoaderContainer.objectLoader.copyTo(output);
    } finally {
      closableObjectLoaderContainer.close();
    }
  }

  private ClosableObjectLoaderContainer getLoader(Repository repo, ObjectId revId,
                  String path)
          throws IOException, RepositoryException
  {
    TreeWalk treeWalk = null;
    RevWalk revWalk = null;

    try
    {
      treeWalk = new TreeWalk(repo);
      treeWalk.setRecursive(Util.nonNull(path).contains("/"));

      if (logger.isDebugEnabled())
      {
        logger.debug("load content for {} at {}", path, revId.name());
      }

      revWalk = new RevWalk(repo);

      RevCommit entry = revWalk.parseCommit(revId);
      RevTree revTree = entry.getTree();

      if (revTree != null)
      {
        treeWalk.addTree(revTree);
      }
      else
      {
        logger.error("could not find tree for {}", revId.name());
      }

      treeWalk.setFilter(PathFilter.create(path));

      if (treeWalk.next() && treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB)
      {
        ObjectId blobId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(blobId);

        return new ClosableObjectLoaderContainer(loader, treeWalk, revWalk);
      }
      else
      {
        throw new PathNotFoundException(path);
      }
    }
    finally
    {
    }
  }

  private static class ClosableObjectLoaderContainer implements Closeable {
    private final ObjectLoader objectLoader;
    private final TreeWalk treeWalk;
    private final RevWalk revWalk;

    private ClosableObjectLoaderContainer(ObjectLoader objectLoader, TreeWalk treeWalk, RevWalk revWalk) {
      this.objectLoader = objectLoader;
      this.treeWalk = treeWalk;
      this.revWalk = revWalk;
    }

    @Override
    public void close() throws IOException {
      GitUtil.release(revWalk);
      GitUtil.release(treeWalk);
    }
  }

  private static class InputStreamWrapper extends InputStream {

    private final InputStream delegate;
    private final TreeWalk treeWalk;
    private final RevWalk revWalk;

    private InputStreamWrapper(ClosableObjectLoaderContainer container) throws IOException {
      this(container.objectLoader.openStream(), container.treeWalk, container.revWalk);
    }

    private InputStreamWrapper(InputStream delegate, TreeWalk treeWalk, RevWalk revWalk) {
        this.delegate = delegate;
        this.treeWalk = treeWalk;
        this.revWalk = revWalk;
      }

      @Override
      public int read () throws IOException {
        return delegate.read();
      }

      @Override
      public void close () throws IOException {
        GitUtil.release(revWalk);
        GitUtil.release(treeWalk);
      }
    }
}
