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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.SvnUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;


public class SvnCatCommand extends AbstractSvnCommand implements CatCommand {

 
  private static final Logger logger =
    LoggerFactory.getLogger(SvnCatCommand.class);


  SvnCatCommand(SvnContext context) {
    super(context);
  }


  @Override
  public void getCatResult(CatCommandRequest request, OutputStream output) {
    if (logger.isDebugEnabled()) {
      logger.debug("try to get content for {}", request);
    }

    String revision = request.getRevision();

    if (SvnUtil.isTransactionEntryId(revision)) {
      String txn = SvnUtil.getTransactionId(revision);

      getCatFromTransaction(request, output, txn);
    } else {

      long revisionNumber = SvnUtil.getRevisionNumber(revision, repository);

      getCatFromRevision(request, output, revisionNumber);
    }
  }

  @Override
  public InputStream getCatResultStream(CatCommandRequest request) {
    // There seems to be no method creating an input stream as a result, so
    // we have no other possibility then to copy the content into a buffer and
    // stream it from there.
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    getCatResult(request, output);
    return new ByteArrayInputStream(output.toByteArray());
  }

  private void getCatFromRevision(CatCommandRequest request, OutputStream output, long revision) {
    logger.debug("try to read content from revision {} and path {}", revision,
      request.getPath());

    try {
      SVNRepository svnRepository = open();

      svnRepository.getFile(request.getPath(), revision, new SVNProperties(),
        output);
    } catch (SVNException ex) {
      handleSvnException(request, ex);
    }
  }

  private void handleSvnException(CatCommandRequest request, SVNException ex) {
    int svnErrorCode = ex.getErrorMessage().getErrorCode().getCode();
    if (SVNErrorCode.FS_NOT_FOUND.getCode() == svnErrorCode) {
      throw notFound(entity("Path", request.getPath()).in("Revision", request.getRevision()).in(repository));
    } else if (SVNErrorCode.FS_NO_SUCH_REVISION.getCode() == svnErrorCode) {
      throw notFound(entity("Revision", request.getRevision()).in(repository));
    } else if (SVNErrorCode.FS_NOT_FILE.getCode() == svnErrorCode) {
      logger.debug("Skip cat command for non-file node");
    } else {
      throw new InternalRepositoryException(repository, "could not get content from revision", ex);
    }
  }

  private void getCatFromTransaction(CatCommandRequest request, OutputStream output, String txn) {
    logger.debug("try to read content from transaction {} and path {}", txn,
      request.getPath());

    SVNClientManager cm = null;

    try {
      cm = SVNClientManager.newInstance();

      SVNLookClient client = cm.getLookClient();

      client.doCat(context.getDirectory(), request.getPath(), txn, output);
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not get content from transaction", ex);
    } finally {
      SvnUtil.dispose(cm);
    }
  }
}
