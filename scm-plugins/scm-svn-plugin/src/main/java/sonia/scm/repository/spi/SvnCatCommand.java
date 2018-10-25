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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnCatCommand extends AbstractSvnCommand implements CatCommand
{

  /**
   * the logger for SvnCatCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnCatCommand.class);

  //~--- constructors ---------------------------------------------------------

  SvnCatCommand(SvnContext context, Repository repository)
  {
    super(context, repository);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public void getCatResult(CatCommandRequest request, OutputStream output) {
    if (logger.isDebugEnabled())
    {
      logger.debug("try to get content for {}", request);
    }

    String revision = request.getRevision();

    if (SvnUtil.isTransactionEntryId(revision))
    {
      String txn = SvnUtil.getTransactionId(revision);

      getCatFromTransaction(request, output, txn);
    }
    else
    {

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

    try
    {
      SVNRepository svnRepository = open();

      svnRepository.getFile(request.getPath(), revision, new SVNProperties(),
        output);
    }
    catch (SVNException ex)
    {
      handleSvnException(request, ex);
    }
  }

  private void handleSvnException(CatCommandRequest request, SVNException ex) {
    int svnErrorCode = ex.getErrorMessage().getErrorCode().getCode();
    if (SVNErrorCode.FS_NOT_FOUND.getCode() == svnErrorCode) {
      throw notFound(entity("Path", request.getPath()).in("Revision", request.getRevision()).in(repository));
    } else if (SVNErrorCode.FS_NO_SUCH_REVISION.getCode() == svnErrorCode) {
      throw notFound(entity("Revision", request.getRevision()).in(repository));
    } else {
      throw new InternalRepositoryException("could not get content from revision", ex);
    }
  }

  private void getCatFromTransaction(CatCommandRequest request, OutputStream output, String txn) {
    logger.debug("try to read content from transaction {} and path {}", txn,
      request.getPath());

    SVNClientManager cm = null;

    try
    {
      cm = SVNClientManager.newInstance();

      SVNLookClient client = cm.getLookClient();

      client.doCat(context.getDirectory(), request.getPath(), txn, output);
    }
    catch (SVNException ex)
    {
      throw new InternalRepositoryException("could not get content from transaction", ex);
    }
    finally
    {
      SvnUtil.dispose(cm);
    }
  }
}
