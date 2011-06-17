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

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryBrowser implements RepositoryBrowser
{

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
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public InputStream getContent(String revision, String path)
          throws IOException, RepositoryException
  {
    throw new UnsupportedOperationException("Not supported yet.");
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
    org.eclipse.jgit.lib.Repository repo =
      RepositoryCache.open(RepositoryCache.FileKey.lenient(directory,
        FS.DETECTED), true);

    try
    {

      ObjectId revId = getRevisionId(repo, revision);

      DirCache cache = new DirCache(directory, FS.DETECTED);
      TreeWalk treeWalk = new TreeWalk(repo);

      treeWalk.addTree(new RevWalk(repo).parseTree(revId));
      treeWalk.addTree(new DirCacheIterator(cache));
      result = new BrowserResult();

      FileObject root = new FileObject();

      root.setDirectory(true);
      root.setPath(Util.nonNull(path));

      List<FileObject> files = new ArrayList<FileObject>();

      root.setChildren(files);
      result.setFile(root);
      result.setRevision(revId.getName());

      while (treeWalk.next())
      {
        files.add(createFileObject(repo, treeWalk));
      }
    }
    finally
    {
      if (repo != null)
      {
        repo.close();
      }
    }

    return result;
  }
  
  
  private ObjectId getRevisionId( org.eclipse.jgit.lib.Repository repo, String revision ) throws IOException{
          ObjectId revId = null;

      if (Util.isNotEmpty(revision))
      {
        revId = repo.resolve(revision);
      }
      else
      {
        revId = repo.resolve(Constants.HEAD);
      }
      return revId;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repo
   * @param treeWalk
   *
   * @return
   *
   * @throws IOException
   * @throws MissingObjectException
   */
  private FileObject createFileObject(org.eclipse.jgit.lib.Repository repo,
          TreeWalk treeWalk)
          throws IOException
  {
    FileObject file = new FileObject();

    file.setName(treeWalk.getNameString());
    file.setPath(treeWalk.getPathString());

    ObjectLoader loader = repo.open(treeWalk.getObjectId(0));

    file.setDirectory(loader.getType() == Constants.OBJ_TREE);
    file.setLength(loader.getSize());

    // TODO
    file.setLastModified(System.currentTimeMillis());

    return file;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler handler;

  /** Field description */
  private Repository repository;
}
