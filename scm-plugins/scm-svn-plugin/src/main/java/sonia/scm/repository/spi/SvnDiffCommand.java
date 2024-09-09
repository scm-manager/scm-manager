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


import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc2.ng.SvnNewDiffGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffOptions;
import org.tmatesoft.svn.core.wc.SVNRevision;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;
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
        SCMSvnDiffGenerator generator = new SCMSvnDiffGenerator();
        diffClient.setDiffGenerator(new SvnNewDiffGenerator(generator));
        if (request.getIgnoreWhitespaceLevel() == IgnoreWhitespaceLevel.ALL) {
          generator.setDiffOptions(new SVNDiffOptions(true, true, true));
        }
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
