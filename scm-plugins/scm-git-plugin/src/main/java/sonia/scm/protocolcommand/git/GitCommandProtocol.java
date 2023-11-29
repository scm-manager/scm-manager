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

package sonia.scm.protocolcommand.git;

import jakarta.inject.Inject;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.protocolcommand.CommandContext;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.protocolcommand.ScmCommandProtocol;
import sonia.scm.repository.RepositoryPermissions;

import java.io.IOException;

@Extension
public class GitCommandProtocol implements ScmCommandProtocol {

  private static final Logger LOG = LoggerFactory.getLogger(GitCommandProtocol.class);

  private final ScmUploadPackFactory uploadPackFactory;
  private final ScmReceivePackFactory receivePackFactory;

  @Inject
  public GitCommandProtocol(ScmUploadPackFactory uploadPackFactory, ScmReceivePackFactory receivePackFactory) {
    this.uploadPackFactory = uploadPackFactory;
    this.receivePackFactory = receivePackFactory;
  }

  @Override
  public void handle(CommandContext commandContext, RepositoryContext repositoryContext) throws IOException {
    String subCommand = commandContext.getArgs()[0];
    if (RemoteConfig.DEFAULT_UPLOAD_PACK.equals(subCommand)) {
      LOG.trace("got upload pack");
      upload(commandContext, repositoryContext);
    } else if (RemoteConfig.DEFAULT_RECEIVE_PACK.equals(subCommand)) {
      LOG.trace("got receive pack");
      receive(commandContext, repositoryContext);
    } else {
      throw new IllegalArgumentException("Unknown git command: " + commandContext.getCommand());
    }
  }

  private void receive(CommandContext commandContext, RepositoryContext repositoryContext) throws IOException {
    RepositoryPermissions.push(repositoryContext.getRepository()).check();
    try (Repository repository = open(repositoryContext)) {
      ReceivePack receivePack = receivePackFactory.create(repositoryContext, repository);
      receivePack.receive(commandContext.getInputStream(), commandContext.getOutputStream(), commandContext.getErrorStream());
    } catch (ServiceNotEnabledException | ServiceNotAuthorizedException e) {
      throw new IOException("error creating receive pack for ssh", e);
    }
  }

  private void upload(CommandContext commandContext, RepositoryContext repositoryContext) throws IOException {
    RepositoryPermissions.pull(repositoryContext.getRepository()).check();
    try (Repository repository = open(repositoryContext)) {
      UploadPack uploadPack = uploadPackFactory.create(repositoryContext, repository);
      uploadPack.upload(commandContext.getInputStream(), commandContext.getOutputStream(), commandContext.getErrorStream());
    }
  }

  private Repository open(RepositoryContext repositoryContext) throws IOException {
    RepositoryCache.FileKey key = RepositoryCache.FileKey.lenient(repositoryContext.getDirectory().toFile(), FS.DETECTED);
    return key.open(true);
  }

}
