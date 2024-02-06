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
