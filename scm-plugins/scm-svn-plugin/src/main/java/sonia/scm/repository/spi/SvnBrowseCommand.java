/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.spi;


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
import sonia.scm.repository.SubRepository;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.tmatesoft.svn.core.SVNErrorCode.FS_NO_SUCH_REVISION;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;


public class SvnBrowseCommand extends AbstractSvnCommand
  implements BrowseCommand {

 
  private static final Logger logger =
    LoggerFactory.getLogger(SvnBrowseCommand.class);

  private int resultCount = 0;

  SvnBrowseCommand(SvnContext context) {
    super(context);
  }

  @Override
  @SuppressWarnings("unchecked")
  public BrowserResult getBrowserResult(BrowseCommandRequest request) {
    String path = Strings.nullToEmpty(request.getPath());
    long revisionNumber = SvnUtil.getRevisionNumber(request.getRevision(), repository);

    if (logger.isDebugEnabled()) {
      logger.debug("browser repository {} in path \"{}\" at revision {}", repository, path, revisionNumber);
    }

    resultCount = 0;

    BrowserResult result = null;

    try {
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
    } catch (SVNException ex) {
      if (FS_NO_SUCH_REVISION.equals(ex.getErrorMessage().getErrorCode())) {
        throw notFound(entity("Revision", Long.toString(revisionNumber)).in(this.repository));
      }
      logger.error("could not open repository: " + repository.getNamespaceAndName(), ex);
    }

    return result;
  }


  @SuppressWarnings("unchecked")
  private void traverse(SVNRepository svnRepository, long revisionNumber, BrowseCommandRequest request,
                        FileObject parent, String basePath)
    throws SVNException {
    List<SVNDirEntry> entries = new ArrayList<>(svnRepository.getDir(parent.getPath(), revisionNumber, null, (Collection) null));
    sort(entries, entry -> entry.getKind() == SVNNodeKind.DIR, SVNDirEntry::getName);
    for (Iterator<SVNDirEntry> iterator = entries.iterator(); resultCount < request.getLimit() + request.getOffset() && iterator.hasNext(); ) {
      SVNDirEntry entry = iterator.next();
      FileObject child = createFileObject(request, svnRepository, revisionNumber, entry, basePath);

      if (!child.isDirectory()) {
        ++resultCount;
      }

      if (child.isDirectory() && request.isRecursive()) {
        traverse(svnRepository, revisionNumber, request, child, createBasePath(child.getPath()));
      }

      if (resultCount > request.getOffset() || (request.getOffset() == 0 && child.isDirectory())) {
        parent.addChild(child);
      }
    }
    if (resultCount >= request.getLimit() + request.getOffset()) {
      parent.setTruncated(true);
    }
  }

  private String createBasePath(String path) {
    String basePath = Util.EMPTY_STRING;

    if (Util.isNotEmpty(path)) {
      basePath = path;

      if (!basePath.endsWith("/")) {
        basePath = basePath.concat("/");
      }
    }

    return basePath;
  }

  private FileObject createFileObject(BrowseCommandRequest request,
                                      SVNRepository repository, long revision, SVNDirEntry entry, String path) {
    if (entry == null) {
      throw notFound(entity("Path", path).in("Revision", Long.toString(revision)).in(this.repository));
    }
    FileObject fileObject = new FileObject();

    fileObject.setName(entry.getName());
    fileObject.setPath(path.concat(entry.getRelativePath()));
    fileObject.setDirectory(entry.getKind() == SVNNodeKind.DIR);

    if (!request.isDisableLastCommit()) {
      if (entry.getDate() != null) {
        fileObject.setCommitDate(entry.getDate().getTime());
      }

      fileObject.setDescription(entry.getCommitMessage());
    }

    fileObject.setLength(entry.getSize());

    if (!request.isDisableSubRepositoryDetection() && fileObject.isDirectory()
      && entry.hasProperties()) {
      fetchExternalsProperty(repository, revision, entry, fileObject);
    }

    return fileObject;
  }

  private boolean shouldSetExternal(String external) {
    return (external.startsWith("http://") || external.startsWith("https://") || external.startsWith("../")
      || external.startsWith("^/") || external.startsWith("/"));
  }

  private void fetchExternalsProperty(SVNRepository repository, long revision,
                                      SVNDirEntry entry, FileObject fileObject) {
    try {
      SVNProperties properties = new SVNProperties();

      repository.getDir(entry.getRelativePath(), revision, properties, (Collection) null);

      String externals = properties.getStringValue(SVNProperty.EXTERNALS);

      if (!Strings.isNullOrEmpty(externals)) {
        String[] splitExternals = externals.split("\\r?\\n");
        for (String external : splitExternals) {
          String subRepoUrl = "";
          String subRepoPath = "";
          for (String externalPart : external.split(" ")) {
            if (shouldSetExternal(externalPart)) {
              subRepoUrl = externalPart;
            } else if (!externalPart.contains("-r")) {
              subRepoPath = externalPart;
            }
          }

          if (Util.isNotEmpty(external)) {
            SubRepository subRepository = new SubRepository(subRepoUrl);
            fileObject.addChild(createSubRepoDirectory(subRepository, subRepoPath));
          }
        }
      }

    } catch (SVNException ex) {
      logger.error("could not fetch file properties", ex);
    }
  }

  private FileObject createSubRepoDirectory(SubRepository subRepository, String subRepoPath) {
    FileObject subRepositoryDirectory = new FileObject();
    subRepositoryDirectory.setPath(subRepoPath);
    subRepositoryDirectory.setName(subRepoPath);
    subRepositoryDirectory.setDirectory(true);
    subRepositoryDirectory.setSubRepository(subRepository);
    return subRepositoryDirectory;
  }
}
