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

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnRepositoryBrowser implements RepositoryBrowser
{

  /** the logger for SvnRepositoryBrowser */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnRepositoryBrowser.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   */
  public SvnRepositoryBrowser(SvnRepositoryHandler handler,
                              Repository repository)
  {
    this.handler = handler;
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * http://wiki.svnkit.com/Printing_Out_File_Contents
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
    long revisionNumber = getRevisionNumber(revision);
    SVNRepository svnRepository = null;

    try
    {
      svnRepository = getSvnRepository();
      svnRepository.getFile(path, revisionNumber, new SVNProperties(), output);
    }
    catch (SVNException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      SvnUtil.closeSession(svnRepository);
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
    long revisionNumber = getRevisionNumber(revision);

    if (logger.isDebugEnabled())
    {
      logger.debug("browser repository {} in path {} at revision {}",
                   new Object[] { repository.getName(),
                                  path, revision });
    }

    BrowserResult result = null;
    SVNRepository svnRepository = null;

    try
    {
      svnRepository = getSvnRepository();

      Collection<SVNDirEntry> entries =
        svnRepository.getDir(Util.nonNull(path), revisionNumber, null,
                             (Collection) null);
      List<FileObject> children = new ArrayList<FileObject>();
      String basePath = Util.EMPTY_STRING;

      if (Util.isNotEmpty(path))
      {
        basePath = path;

        if (!basePath.endsWith("/"))
        {
          basePath = basePath.concat("/");
        }
      }

      for (SVNDirEntry entry : entries)
      {
        children.add(createFileObject(svnRepository, revisionNumber, entry,
                                      basePath));
      }

      result = new BrowserResult();
      result.setRevision(revision);
      result.setFiles(children);
    }
    catch (SVNException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      SvnUtil.closeSession(svnRepository);
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param repository
   * @param revision
   * @param entry
   * @param path
   *
   * @return
   */
  private FileObject createFileObject(SVNRepository repository, long revision,
          SVNDirEntry entry, String path)
  {
    FileObject fileObject = new FileObject();

    fileObject.setName(entry.getName());
    fileObject.setPath(path.concat(entry.getRelativePath()));
    fileObject.setDirectory(entry.getKind() == SVNNodeKind.DIR);

    if (entry.getDate() != null)
    {
      fileObject.setLastModified(entry.getDate().getTime());
    }

    fileObject.setLength(entry.getSize());
    fileObject.setDescription(entry.getCommitMessage());

    if (fileObject.isDirectory() && entry.hasProperties())
    {
      fetchExternalsProperty(repository, revision, entry, fileObject);
    }

    return fileObject;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param revision
   * @param entry
   * @param fileObject
   */
  private void fetchExternalsProperty(SVNRepository repository, long revision,
          SVNDirEntry entry, FileObject fileObject)
  {
    try
    {
      SVNProperties properties = new SVNProperties();

      repository.getFile(entry.getRelativePath(), revision, properties, null);

      String externals = properties.getStringValue(SVNProperty.EXTERNALS);

      if (Util.isNotEmpty(externals))
      {
        SubRepository subRepository = new SubRepository(externals);

        fileObject.setSubRepository(subRepository);
      }
    }
    catch (SVNException ex)
    {
      logger.error("could not fetch file properties");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   *
   * @throws RepositoryException
   */
  private long getRevisionNumber(String revision) throws RepositoryException
  {
    long revisionNumber = -1;

    if (Util.isNotEmpty(revision))
    {
      try
      {
        revisionNumber = Long.parseLong(revision);
      }
      catch (NumberFormatException ex)
      {
        throw new RepositoryException("given revision is not a svnrevision");
      }
    }

    return revisionNumber;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws SVNException
   */
  private SVNRepository getSvnRepository() throws SVNException
  {
    File directory = handler.getDirectory(repository);

    return SVNRepositoryFactory.create(SVNURL.fromFile(directory));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SvnRepositoryHandler handler;

  /** Field description */
  private Repository repository;
}
