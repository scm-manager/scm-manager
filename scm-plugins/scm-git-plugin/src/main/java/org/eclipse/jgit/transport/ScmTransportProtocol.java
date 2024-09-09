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

package org.eclipse.jgit.transport;


import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.web.CollectingPackParserListener;
import sonia.scm.web.GitReceiveHook;

import java.io.File;
import java.util.Set;


public class ScmTransportProtocol extends TransportProtocol {

  public static final String NAME = "scm";
  private static final Set<String> SCHEMES = ImmutableSet.of(NAME);

  private Provider<GitChangesetConverterFactory> converterFactory;
  private Provider<HookEventFacade> hookEventFacadeProvider;
  private Provider<GitRepositoryHandler> repositoryHandlerProvider;

  public ScmTransportProtocol() {
  }

  @Inject
  public ScmTransportProtocol(
    Provider<GitChangesetConverterFactory> converterFactory,
    Provider<HookEventFacade> hookEventFacadeProvider,
    Provider<GitRepositoryHandler> repositoryHandlerProvider) {
    this.converterFactory = converterFactory;
    this.hookEventFacadeProvider = hookEventFacadeProvider;
    this.repositoryHandlerProvider = repositoryHandlerProvider;
  }

  @Override
  public boolean canHandle(URIish uri, Repository local, String remoteName) {
    return (uri.getPath() != null) && (uri.getPort() <= 0)
      && (uri.getUser() == null) && (uri.getPass() == null)
      && (uri.getHost() == null)
      && ((uri.getScheme() == null) || getSchemes().contains(uri.getScheme()));
  }

  @Override
  public Transport open(URIish uri, Repository local, String remoteName) throws TransportException {
    File localDirectory = local.getDirectory();
    File path = local.getFS().resolve(localDirectory, uri.getPath());
    File gitDir = RepositoryCache.FileKey.resolve(path, local.getFS());

    if (gitDir == null) {
      throw new NoRemoteRepositoryException(uri, JGitText.get().notFound);
    }

    return new TransportLocalWithHooks(
      converterFactory.get(),
      hookEventFacadeProvider.get(),
      repositoryHandlerProvider.get(),
      local, uri, gitDir
    );
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Set<String> getSchemes() {
    return SCHEMES;
  }

  private static class TransportLocalWithHooks extends TransportLocal {

    private final GitChangesetConverterFactory converterFactory;
    private final GitRepositoryHandler handler;
    private final HookEventFacade hookEventFacade;

    public TransportLocalWithHooks(
      GitChangesetConverterFactory converterFactory,
      HookEventFacade hookEventFacade,
      GitRepositoryHandler handler,
      Repository local, URIish uri, File gitDir) {
      super(local, uri, gitDir);
      this.converterFactory = converterFactory;
      this.hookEventFacade = hookEventFacade;
      this.handler = handler;
    }

    @Override
    ReceivePack createReceivePack(Repository dst) {
      ReceivePack pack = new ReceivePack(dst);

      if ((hookEventFacade != null) && (handler != null) && (converterFactory != null)) {
        GitReceiveHook hook = new GitReceiveHook(converterFactory, hookEventFacade, handler);

        pack.setPreReceiveHook(hook);
        pack.setPostReceiveHook(hook);

        CollectingPackParserListener.set(pack, hook);
      }

      return pack;
    }
  }

}
