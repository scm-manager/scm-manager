/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

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

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
public class SvnCatCommand extends AbstractSvnCommand implements CatCommand {

  /**
   * the logger for SvnCatCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnCatCommand.class);

  //~--- constructors ---------------------------------------------------------

  SvnCatCommand(SvnContext context) {
    super(context);
  }

  //~--- get methods ----------------------------------------------------------

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
