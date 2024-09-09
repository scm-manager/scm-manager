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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.HealthCheckResult;

public class SvnFullHealthCheckCommand extends AbstractSvnCommand implements FullHealthCheckCommand{

  private static final Logger LOG = LoggerFactory.getLogger(SvnFullHealthCheckCommand.class);

  protected SvnFullHealthCheckCommand(SvnContext context) {
    super(context);
  }

  @Override
  public HealthCheckResult check() {
    SVNClientManager clientManager= SVNClientManager.newInstance();
    SVNAdminClient adminClient = clientManager.getAdminClient();
    try {
      adminClient.doVerify(context.getDirectory());
    } catch (SVNException e) {
      LOG.warn("svn verify failed for repository {}", context.get(), e);
      return HealthCheckResult.unhealthy(new HealthCheckFailure("5FSV2kreE1",
        "svn verify failed", "The check 'svn verify' failed for the repository."));
    }

    return HealthCheckResult.healthy();
  }
}
