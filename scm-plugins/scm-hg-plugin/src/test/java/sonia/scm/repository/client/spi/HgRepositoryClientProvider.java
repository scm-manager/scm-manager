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

import org.javahg.Repository;
import com.google.common.collect.ImmutableSet;
import sonia.scm.repository.client.api.ClientCommand;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Mercurial implementation of the {@link RepositoryClientProvider}.
 * 
 */
public class HgRepositoryClientProvider extends RepositoryClientProvider
{
  
  private static final Set<ClientCommand> SUPPORTED_COMMANDS = ImmutableSet.of(
    ClientCommand.ADD, ClientCommand.REMOVE, ClientCommand.COMMIT, 
    ClientCommand.TAG, ClientCommand.BRANCH, ClientCommand.PUSH
  );
  
  private final Repository repository;
  private final File hgrc;
  private final String url;

  HgRepositoryClientProvider(Repository repository, File hgrc, String url)
  {
    this.repository = repository;
    this.hgrc = hgrc;
    this.url = url;
  }

  @Override
  public Set<ClientCommand> getSupportedClientCommands()
  {
    return SUPPORTED_COMMANDS;
  }

  @Override
  public AddCommand getAddCommand()
  {
    return new HgAddCommand(repository);
  }

  @Override
  public RemoveCommand getRemoveCommand()
  {
    return new HgRemoveCommand(repository);
  }

  @Override
  public CommitCommand getCommitCommand()
  {
    return new HgCommitCommand(repository);
  }

  @Override
  public TagCommand getTagCommand()
  {
    return new HgTagCommand(repository);
  }

  @Override
  public BranchCommand getBranchCommand()
  {
    return new HgBranchCommand(repository);
  }

  @Override
  public PushCommand getPushCommand()
  {
    return new HgPushCommand(repository, url);
  }

  @Override
  public File getWorkingCopy() {
    return repository.getDirectory();
  }

  @Override
  public void close() throws IOException
  {
    if ( hgrc != null && hgrc.exists() && ! hgrc.delete() ){
      throw new IOException("failed to remove hgrc file ".concat(hgrc.getPath()));
    }
    repository.close();
  }
}
