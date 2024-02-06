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
