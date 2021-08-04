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

import lombok.extern.slf4j.Slf4j;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
public class SvnModificationsCommand extends AbstractSvnCommand implements ModificationsCommand {

  SvnModificationsCommand(SvnContext context) {
    super(context);
  }

  @Override
  public Modifications getModifications(String revisionOrTransactionId) {
    Modifications modifications;
    try {
      if (SvnUtil.isTransactionEntryId(revisionOrTransactionId)) {
        modifications = getModificationsFromTransaction(SvnUtil.getTransactionId(revisionOrTransactionId));
      } else {
        modifications = getModificationFromRevision(revisionOrTransactionId);
      }
      return modifications;
    } catch (SVNException ex) {
      throw new InternalRepositoryException(
        repository,
        "failed to get svn modifications for " + revisionOrTransactionId,
        ex
      );
    }
  }

  @SuppressWarnings("unchecked")
  private Modifications getModificationFromRevision(String revision) throws SVNException {
    log.debug("get svn modifications from revision: {}", revision);
    long revisionNumber = SvnUtil.getRevisionNumber(revision, repository);
    SVNRepository repo = open();
    Collection<SVNLogEntry> entries = repo.log(null, null, revisionNumber,
      revisionNumber, true, true);
    if (Util.isNotEmpty(entries)) {
      return SvnUtil.createModifications(entries.iterator().next(), revision);
    }
    return null;
  }

  private Modifications getModificationsFromTransaction(String transaction) throws SVNException {
    log.debug("get svn modifications from transaction: {}", transaction);
    SVNLookClient client = SVNClientManager.newInstance().getLookClient();
    Collection<Modification> modificationList = new ArrayList<>();
    client.doGetChanged(context.getDirectory(), transaction,
      e -> SvnUtil.asModification(e.getType(), e.getPath()).ifPresent(modificationList::add), true);

    return new Modifications(null, modificationList);
  }
}
