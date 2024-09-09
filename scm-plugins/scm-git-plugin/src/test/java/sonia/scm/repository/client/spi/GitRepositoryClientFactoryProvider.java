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
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.client.api.RepositoryClientException;

import java.io.File;
import java.io.IOException;


public class GitRepositoryClientFactoryProvider
  implements RepositoryClientFactoryProvider
{

  /**
   * Method description
   *
   *
   * @param main
   * @param workingCopy
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public RepositoryClientProvider create(File main, File workingCopy)
    throws IOException
  {
    Git git = null;

    try
    {
      git = Git.cloneRepository().setURI(main.toURI().toString()).setDirectory(
        workingCopy).call();
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryClientException("could not clone repository", ex);
    }

    return new GitRepositoryClientProvider(git);
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param username
   * @param password
   * @param workingCopy
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public RepositoryClientProvider create(String url, String username,
    String password, File workingCopy)
    throws IOException
  {
    Git git = null;

    CredentialsProvider credentialsProvider = null;
    if ( username != null && password != null ) {
      credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
    }

    try
    {
      git = Git.cloneRepository()
        .setURI(url)
        .setDirectory(workingCopy)
        .setCredentialsProvider(credentialsProvider)
        .call();
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryClientException("could not clone repository", ex);
    }

    return new GitRepositoryClientProvider(git, credentialsProvider);
  }


  
  @Override
  public String getType()
  {
    return GitRepositoryHandler.TYPE_NAME;
  }
}
