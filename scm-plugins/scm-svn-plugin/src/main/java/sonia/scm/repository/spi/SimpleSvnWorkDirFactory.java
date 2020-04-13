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
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnWorkDirFactory;
import sonia.scm.repository.util.CacheSupportingWorkdirProvider;
import sonia.scm.repository.util.SimpleWorkdirFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class SimpleSvnWorkDirFactory extends SimpleWorkdirFactory<File, File, SvnContext> implements SvnWorkDirFactory {

  @Inject
  public SimpleSvnWorkDirFactory(CacheSupportingWorkdirProvider workdirProvider) {
    super(workdirProvider);
  }

  @Override
  protected Repository getScmRepository(SvnContext context) {
    return context.getRepository();
  }

  @Override
  protected ParentAndClone<File, File> cloneRepository(SvnContext context, File workingCopy, String initialBranch) {

    final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();

    SVNURL source;
    try {
      source = SVNURL.fromFile(context.getDirectory());
    } catch (SVNException ex) {
      throw new InternalRepositoryException(getScmRepository(context), "error creating svn url from central directory", ex);
    }

    try {
      final SvnCheckout checkout = svnOperationFactory.createCheckout();
      checkout.setSingleTarget(SvnTarget.fromFile(workingCopy));
      checkout.setSource(SvnTarget.fromURL(source));
      checkout.run();
    } catch (SVNException ex) {
      throw new InternalRepositoryException(getScmRepository(context), "error running svn checkout", ex);
    } finally {
      svnOperationFactory.dispose();
    }

    return new ParentAndClone<>(context.getDirectory(), workingCopy, workingCopy);
  }

  @Override
  protected ParentAndClone<File, File> reclaimRepository(SvnContext context, File target, String initialBranch) throws IOException {
    return new ParentAndClone<>(context.getDirectory(), target, target);
  }

  @Override
  protected void closeRepository(File workingCopy) {
  }

  @Override
  protected void closeWorkdirInternal(File workdir) {
  }
}
