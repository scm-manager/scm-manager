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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import sonia.scm.repository.client.api.RepositoryClientException;

import java.io.IOException;
import java.util.function.Supplier;


public class GitPushCommand implements PushCommand
{

 
  public GitPushCommand(Git git, CredentialsProvider credentialsProvider)
  {
    this.git = git;
    this.credentialsProvider = credentialsProvider;
  }



  @Override
  public void push() throws IOException {
    push(() -> git.push().setPushAll());
  }

  @Override
  public void pushTags() throws IOException {
    push(() -> git.push().setPushTags());
  }

  private void push(Supplier<org.eclipse.jgit.api.PushCommand> commandSupplier) throws RepositoryClientException
  {
    try
    {
      org.eclipse.jgit.api.PushCommand cmd = commandSupplier.get();

      if (credentialsProvider != null)
      {
        cmd.setCredentialsProvider(credentialsProvider);
      }

      cmd.call();
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryClientException(
        "could not push to remote repository", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  private CredentialsProvider credentialsProvider;

  private Git git;
}
