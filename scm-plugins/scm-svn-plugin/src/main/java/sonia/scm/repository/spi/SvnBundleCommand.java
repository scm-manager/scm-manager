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


import com.google.common.io.ByteSink;
import com.google.common.io.Closeables;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.BundleResponse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public class SvnBundleCommand extends AbstractSvnCommand
  implements BundleCommand {

  private static final String DUMP = "dump";

  public SvnBundleCommand(SvnContext context) {
    super(context);
  }

  private static void dump(SVNAdminClient adminClient, File repository,
                           ByteSink target)
    throws SVNException, IOException {
    OutputStream outputStream = null;

    try {
      outputStream = target.openBufferedStream();
      adminClient.doDump(repository, outputStream, SVNRevision.create(-1l),
        SVNRevision.HEAD, false, false);
    } finally {
      Closeables.close(outputStream, true);
    }
  }

  @Override
  public BundleResponse bundle(BundleCommandRequest request) throws IOException {
    ByteSink archive = checkNotNull(request.getArchive(),
      "archive is required");

    BundleResponse response;

    SVNClientManager clientManager = null;

    try {
      clientManager = SVNClientManager.newInstance();

      SVNAdminClient adminClient = clientManager.getAdminClient();

      dump(adminClient, context.getDirectory(), archive);
      response = new BundleResponse(context.open().getLatestRevision());
    } catch (SVNException ex) {
      throw new IOException("could not create dump", ex);
    } finally {
      SvnUtil.dispose(clientManager);
    }

    return response;
  }

  @Override
  public String getFileExtension() {
    return DUMP;
  }
}
