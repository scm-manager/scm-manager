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

package sonia.scm.repository.client.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import sonia.scm.repository.client.api.RepositoryClientException;

import java.io.IOException;

public class GitDeleteRemoteBranchCommand implements DeleteRemoteBranchCommand {

  private final Git git;
  private final CredentialsProvider credentialsProvider;

  GitDeleteRemoteBranchCommand(Git git, CredentialsProvider credentialsProvider) {
    this.git = git;
    this.credentialsProvider = credentialsProvider;
  }

  @Override
  public void delete(String name) throws IOException {
    try {
      git.branchDelete().setBranchNames("refs/heads/" + name).call();
      RefSpec refSpec = new RefSpec()
        .setSource(null)
        .setDestination("refs/heads/" + name);
      PushCommand push = git.push();
      if (credentialsProvider != null) {
        push.setCredentialsProvider(credentialsProvider);
      }
      push.setRefSpecs(refSpec).call();
    } catch (GitAPIException ex) {
      throw new RepositoryClientException("could not delete branch", ex);
    }
  }
}
