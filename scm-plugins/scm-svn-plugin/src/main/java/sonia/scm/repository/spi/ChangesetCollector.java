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

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNLogEntry;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.SvnUtil;

import java.util.Collection;

/**
 * Collect and convert changesets.
 */
class ChangesetCollector implements ISVNLogEntryHandler {

  private final Collection<Changeset> changesets;

  public ChangesetCollector(Collection<Changeset> changesets) {
    this.changesets = changesets;
  }

  @Override
  public void handleLogEntry(SVNLogEntry logEntry) {
    changesets.add(SvnUtil.createChangeset(logEntry));
  }
}
