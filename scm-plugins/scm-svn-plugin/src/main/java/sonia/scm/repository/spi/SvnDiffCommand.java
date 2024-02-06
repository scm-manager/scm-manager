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


import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc2.ng.SvnNewDiffGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.util.Util;


public class SvnDiffCommand extends AbstractSvnCommand implements DiffCommand {

 
  private static final Logger logger =
    LoggerFactory.getLogger(SvnDiffCommand.class);

  public SvnDiffCommand(SvnContext context) {
    super(context);
  }

  @Override
  public DiffCommandBuilder.OutputStreamConsumer getDiffResult(DiffCommandRequest request) {
    logger.debug("create diff for {}", request);
    Preconditions.checkNotNull(request, "request is required");

    String path = request.getPath();
    return output -> {
      SVNClientManager clientManager = null;
      try {
        SVNURL svnurl = context.createUrl();
        if (Util.isNotEmpty(path)) {
          svnurl = svnurl.appendPath(path, true);
        }
        clientManager = SVNClientManager.newInstance();
        SVNDiffClient diffClient = clientManager.getDiffClient();
        diffClient.setDiffGenerator(new SvnNewDiffGenerator(new SCMSvnDiffGenerator()));

        long currentRev = SvnUtil.getRevisionNumber(request.getRevision(), repository);

        diffClient.setGitDiffFormat(request.getFormat() == DiffFormat.GIT);

        diffClient.doDiff(svnurl, SVNRevision.HEAD,
          SVNRevision.create(currentRev - 1), SVNRevision.create(currentRev),
          SVNDepth.INFINITY, false, output);
      } catch (SVNException ex) {
        throw new InternalRepositoryException(repository, "could not create diff", ex);
      } finally {
        SvnUtil.dispose(clientManager);
      }
    };
  }
}
