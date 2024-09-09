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

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import sonia.scm.repository.work.SimpleWorkingCopyFactory;
import sonia.scm.repository.work.SimpleWorkingCopyFactory.ParentAndClone;

import java.io.File;

import static org.tmatesoft.svn.core.SVNDepth.INFINITY;

class SvnWorkingCopyReclaimer {
  private final SvnContext context;

  public SvnWorkingCopyReclaimer(SvnContext context) {
    this.context = context;
  }

  public ParentAndClone<File, File> reclaim(File target) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    SVNClientManager clientManager = SVNClientManager.newInstance();
    try {
      clientManager.getWCClient().doRevert(new File[] {target}, INFINITY, null);
      clientManager.getWCClient().doCleanup(target, true, true, true, true, true, false);
      clientManager.getUpdateClient().doUpdate(target, SVNRevision.HEAD, INFINITY, false, false);
    } catch (SVNException e) {
      throw new SimpleWorkingCopyFactory.ReclaimFailedException(e);
    }
    return new ParentAndClone<>(context.getDirectory(), target, target);
  }
}
