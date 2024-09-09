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
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.work.SimpleWorkingCopyFactory.ParentAndClone;

import java.io.File;

class SvnWorkingCopyInitializer {
  private final SvnContext context;

  public SvnWorkingCopyInitializer(SvnContext context) {
    this.context = context;
  }

  public ParentAndClone<File, File> initialize(File workingCopy) {
    final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();

    SVNURL source;
    try {
      source = SVNURL.fromFile(context.getDirectory());
    } catch (SVNException ex) {
      throw new InternalRepositoryException(context.getRepository(), "error creating svn url from central directory", ex);
    }

    try {
      final SvnCheckout checkout = svnOperationFactory.createCheckout();
      checkout.setSingleTarget(SvnTarget.fromFile(workingCopy));
      checkout.setSource(SvnTarget.fromURL(source));
      checkout.run();
    } catch (SVNException ex) {
      throw new InternalRepositoryException(context.getRepository(), "error running svn checkout", ex);
    } finally {
      svnOperationFactory.dispose();
    }

    return new ParentAndClone<>(context.getDirectory(), workingCopy, workingCopy);
  }
}
