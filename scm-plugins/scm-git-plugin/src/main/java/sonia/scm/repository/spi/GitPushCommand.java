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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.api.PushResponse;

import java.io.IOException;

public class GitPushCommand extends AbstractGitPushOrPullCommand implements PushCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitPushCommand.class);

  @Inject
  public GitPushCommand(GitRepositoryHandler handler, @Assisted GitContext context) {
    super(handler, context);
    this.handler = handler;
  }

  @Override
  public PushResponse push(PushCommandRequest request)
    throws IOException {
    String remoteUrl = getRemoteUrl(request);

    LOG.debug("push changes from {} to {}", repository, remoteUrl);

    return new PushResponse(push(open(), remoteUrl, request.getUsername(), request.getPassword(), request.isForce()));
  }

  public interface Factory {
    PushCommand create(GitContext context);
  }

}
