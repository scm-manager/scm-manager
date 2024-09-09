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

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.web.CollectingPackParserListener;
import sonia.scm.web.GitReceiveHook;

public abstract class BaseReceivePackFactory<T> implements ReceivePackFactory<T> {

  private final GitChangesetConverterFactory converterFactory;
  private final GitRepositoryHandler handler;
  private final HookEventFacade hookEventFacade;
  private final GitRepositoryConfigStoreProvider storeProvider;

  protected BaseReceivePackFactory(GitChangesetConverterFactory converterFactory,
                                   GitRepositoryHandler handler,
                                   HookEventFacade hookEventFacade,
                                   GitRepositoryConfigStoreProvider storeProvider) {
    this.converterFactory = converterFactory;
    this.handler = handler;
    this.hookEventFacade = hookEventFacade;
    this.storeProvider = storeProvider;
  }

  @Override
  public final ReceivePack create(T connection, Repository repository) throws ServiceNotAuthorizedException, ServiceNotEnabledException {
    ReceivePack receivePack = createBasicReceivePack(connection, repository);
    receivePack.setAllowNonFastForwards(isNonFastForwardAllowed(repository));

    GitReceiveHook hook = new GitReceiveHook(converterFactory, hookEventFacade, handler);
    receivePack.setPreReceiveHook(hook);
    receivePack.setPostReceiveHook(hook);
    // apply collecting listener, to be able to check which commits are new
    CollectingPackParserListener.set(receivePack, hook);

    return receivePack;
  }

  protected abstract ReceivePack createBasicReceivePack(T request, Repository repository)
    throws ServiceNotEnabledException, ServiceNotAuthorizedException;

  private boolean isNonFastForwardAllowed(Repository repository) {
    String repositoryId = handler.getRepositoryId(repository.getConfig());
    GitRepositoryConfig gitRepositoryConfig = storeProvider.getGitRepositoryConfig(repositoryId);
    return !(handler.getConfig().isNonFastForwardDisallowed() || gitRepositoryConfig.isNonFastForwardDisallowed());
  }
}
