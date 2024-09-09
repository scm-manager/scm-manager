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
