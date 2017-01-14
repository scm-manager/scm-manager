/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.repository.client.spi;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import sonia.scm.repository.client.api.ClientCommand;

/**
 * Subversion repository client provider.
 * 
 * @author Sebastian Sdorra
 * @since 1.51
 */
public class SvnRepositoryClientProvider extends RepositoryClientProvider {

  private static final Set<ClientCommand> SUPPORTED_COMMANDS = ImmutableSet.of(
    ClientCommand.ADD, ClientCommand.REMOVE, ClientCommand.COMMIT
  );
  
  private final SVNClientManager client;
  private final File workingCopy;

  private final List<File> addedFiles = new ArrayList<>();
  private final List<File> removedFiles = new ArrayList<>();
  
  SvnRepositoryClientProvider(SVNClientManager client, File workingCopy) {
    this.client = client;
    this.workingCopy = workingCopy;
  }

  @Override
  public SvnAddCommand getAddCommand() {
    return new SvnAddCommand(workingCopy, addedFiles);
  }

  @Override
  public SvnRemoveCommand getRemoveCommand() {
    return new SvnRemoveCommand(workingCopy, removedFiles);
  }

  @Override
  public SvnCommitCommand getCommitCommand() {
    return new SvnCommitCommand(client, workingCopy, addedFiles, removedFiles);
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
