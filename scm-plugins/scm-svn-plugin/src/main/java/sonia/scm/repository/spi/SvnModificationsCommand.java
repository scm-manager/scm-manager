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
    try {
      if (SvnUtil.isTransactionEntryId(revisionOrTransactionId)) {
        return getModificationsFromTransaction(SvnUtil.getTransactionId(revisionOrTransactionId));
      } else {
        return getModificationFromRevision(revisionOrTransactionId, revisionOrTransactionId);
      }
    } catch (SVNException ex) {
      throw new InternalRepositoryException(
        repository,
        "failed to get svn modifications for " + revisionOrTransactionId,
        ex
      );
    }
  }

  @Override
  public Modifications getModifications(String baseRevision, String revision) {
    try {
      return getModificationFromRevision(baseRevision, revision);
    } catch (SVNException ex) {
      throw new InternalRepositoryException(
        repository,
        "failed to get svn modifications from " + baseRevision + " to " + revision,
        ex
      );
    }
  }

  @SuppressWarnings("unchecked")
  private Modifications getModificationFromRevision(String startRevision, String endRevision) throws SVNException {
    log.debug("get svn modifications from revision {} to {}", startRevision, endRevision);
    long startRevisionNumber = SvnUtil.getRevisionNumber(startRevision, repository);
    long endRevisionNumber = SvnUtil.getRevisionNumber(endRevision, repository);
    SVNRepository repo = open();
    Collection<SVNLogEntry> entries = repo.log(null, null, startRevisionNumber,
      endRevisionNumber, true, true);
    if (Util.isNotEmpty(entries)) {
      if (startRevision.equals(endRevision)) {
        return SvnUtil.createModifications(entries.iterator().next(), endRevision);
      } else {
        return SvnUtil.createModifications(startRevision, endRevision, entries);
      }
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
