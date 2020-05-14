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

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import sonia.scm.repository.work.SimpleWorkingCopyFactory;
import sonia.scm.repository.work.SimpleWorkingCopyFactory.ParentAndClone;

import java.io.File;

import static org.tmatesoft.svn.core.SVNDepth.INFINITY;

class SvnWorkingCopyReclaimer implements SimpleWorkingCopyFactory.WorkingCopyReclaimer<File, File> {
  private final SvnContext context;

  public SvnWorkingCopyReclaimer(SvnContext context) {
    this.context = context;
  }

  @Override
  public ParentAndClone<File, File> reclaim(File target, String initialBranch) throws SimpleWorkingCopyFactory.ReclaimFailedException {
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
