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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.io.SVNRepository;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SubRepository;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

import java.util.Collection;

import static org.tmatesoft.svn.core.SVNErrorCode.FS_NO_SUCH_REVISION;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnBrowseCommand extends AbstractSvnCommand
  implements BrowseCommand
{

  /**
   * the logger for SvnBrowseCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnBrowseCommand.class);

  SvnBrowseCommand(SvnContext context, Repository repository)
  {
    super(context, repository);
  }

  @Override
  @SuppressWarnings("unchecked")
  public BrowserResult getBrowserResult(BrowseCommandRequest request) {
    String path = Strings.nullToEmpty(request.getPath());
    long revisionNumber = SvnUtil.getRevisionNumber(request.getRevision(), repository);

    if (logger.isDebugEnabled()) {
      logger.debug("browser repository {} in path \"{}\" at revision {}", repository.getName(), path, revisionNumber);
    }

    BrowserResult result = null;

    try
    {
      SVNRepository svnRepository = open();

      if (revisionNumber == -1) {
        revisionNumber = svnRepository.getLatestRevision();
      }

      SVNDirEntry rootEntry = svnRepository.info(path, revisionNumber);
      FileObject root = createFileObject(request, svnRepository, revisionNumber, rootEntry, path);
      root.setPath(path);

      if (root.isDirectory()) {
        traverse(svnRepository, revisionNumber, request, root, createBasePath(path));
      }


      result = new BrowserResult(String.valueOf(revisionNumber), root);
    }
    catch (SVNException ex)
    {
      if (FS_NO_SUCH_REVISION.equals(ex.getErrorMessage().getErrorCode())) {
        throw notFound(entity("Revision", Long.toString(revisionNumber)).in(this.repository));
      }
      logger.error("could not open repository", ex);
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  @SuppressWarnings("unchecked")
  private void traverse(SVNRepository svnRepository, long revisionNumber, BrowseCommandRequest request,
    FileObject parent, String basePath)
    throws SVNException
  {
    Collection<SVNDirEntry> entries = svnRepository.getDir(parent.getPath(), revisionNumber, null, (Collection) null);
    for (SVNDirEntry entry : entries)
    {
      FileObject child = createFileObject(request, svnRepository, revisionNumber, entry, basePath);

      parent.addChild(child);

      if (child.isDirectory() && request.isRecursive()) {
        traverse(svnRepository, revisionNumber, request, child, createBasePath(child.getPath()));
      }
    }
  }

  private String createBasePath(String path)
  {
    String basePath = Util.EMPTY_STRING;

    if (Util.isNotEmpty(path))
    {
      basePath = path;

      if (!basePath.endsWith("/"))
      {
        basePath = basePath.concat("/");
      }
    }

    return basePath;
  }

  private FileObject createFileObject(BrowseCommandRequest request,
    SVNRepository repository, long revision, SVNDirEntry entry, String path)
  {
    if (entry == null) {
      throw notFound(entity("Path", path).in("Revision", Long.toString(revision)).in(this.repository));
    }
    FileObject fileObject = new FileObject();

    fileObject.setName(entry.getName());
    fileObject.setPath(path.concat(entry.getRelativePath()));
    fileObject.setDirectory(entry.getKind() == SVNNodeKind.DIR);

    if (!request.isDisableLastCommit())
    {
      if (entry.getDate() != null)
      {
        fileObject.setLastModified(entry.getDate().getTime());
      }

      fileObject.setDescription(entry.getCommitMessage());
    }

    fileObject.setLength(entry.getSize());

    if (!request.isDisableSubRepositoryDetection() && fileObject.isDirectory()
      && entry.hasProperties())
    {
      fetchExternalsProperty(repository, revision, entry, fileObject);
    }

    return fileObject;
  }

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
      logger.error("could not fetch file properties", ex);
    }
  }
}
