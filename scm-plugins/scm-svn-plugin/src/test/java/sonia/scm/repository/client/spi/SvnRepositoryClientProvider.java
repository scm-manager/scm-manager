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

import com.google.common.collect.ImmutableSet;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import sonia.scm.repository.client.api.ClientCommand;

import java.io.File;
import java.util.Set;

/**
 * Subversion repository client provider.
 * 
 * @since 1.51
 */
public class SvnRepositoryClientProvider extends RepositoryClientProvider {

  private static final Set<ClientCommand> SUPPORTED_COMMANDS = ImmutableSet.of(
    ClientCommand.ADD, ClientCommand.REMOVE, ClientCommand.COMMIT
  );
  
  private final SVNClientManager client;
  private final File workingCopy;

  private final SvnChangeWorker changeWorker;

  SvnRepositoryClientProvider(SVNClientManager client, File workingCopy) {
    changeWorker = new SvnChangeWorker(workingCopy);
    this.client = client;
    this.workingCopy = workingCopy;
  }

  @Override
  public AddCommand getAddCommand() {
    return changeWorker.addCommand();
  }

  @Override
  public RemoveCommand getRemoveCommand() {
    return changeWorker.removeCommand();
  }

  @Override
  public CommitCommand getCommitCommand() {
    return changeWorker.commitCommand(client);
  }

  @Override
  public File getWorkingCopy() {
    return workingCopy;
  }
  
  @Override
  public Set<ClientCommand> getSupportedClientCommands() {
    return SUPPORTED_COMMANDS;
  }
  
}
