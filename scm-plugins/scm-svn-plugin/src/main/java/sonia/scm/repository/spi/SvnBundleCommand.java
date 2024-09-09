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
