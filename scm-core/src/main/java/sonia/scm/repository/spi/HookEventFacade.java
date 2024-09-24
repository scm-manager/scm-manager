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

package sonia.scm.repository.spi;


import com.google.inject.Inject;
import com.google.inject.Provider;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

/**
 *
 * @since 1.33
 */
public final class HookEventFacade
{
  private final HookContextFactory hookContextFactory;

  private final Provider<RepositoryManager> repositoryManagerProvider;

  /**
   * @since 1.38
   */
  @Inject
  public HookEventFacade(Provider<RepositoryManager> repositoryManagerProvider,
    HookContextFactory hookContextFactory)
  {
    this.repositoryManagerProvider = repositoryManagerProvider;
    this.hookContextFactory = hookContextFactory;
  }


  public HookEventHandler handle(String id) {
    Repository repository = repositoryManagerProvider.get().get(id);
    if (repository == null)
    {
      throw notFound(entity("Repository", id));
    }
    return handle(repository);
  }

  public HookEventHandler handle(NamespaceAndName namespaceAndName) {
    Repository repository = repositoryManagerProvider.get().get(namespaceAndName);
    if (repository == null)
    {
      throw notFound(entity(namespaceAndName));
    }
    return handle(repository);
  }

  public HookEventHandler handle(Repository repository) {
    return new HookEventHandler(repositoryManagerProvider.get(),
      hookContextFactory, repository);
  }




  public static class HookEventHandler
  {
    private final HookContextFactory hookContextFactory;

    private final Repository repository;

    private final RepositoryManager repositoryManager;

    public HookEventHandler(RepositoryManager repositoryManager,
      HookContextFactory hookContextFactory, Repository repository)
    {
      this.repositoryManager = repositoryManager;
      this.hookContextFactory = hookContextFactory;
      this.repository = repository;
    }

    public void fireHookEvent(RepositoryHookType type,
      HookContextProvider hookContextProvider)
    {
      HookContext context =
        hookContextFactory.createContext(hookContextProvider, repository);
      RepositoryHookEvent event = new RepositoryHookEvent(context, repository,
                                    type);

      repositoryManager.fireHookEvent(event);
      hookContextProvider.handleClientDisconnect();
    }

  }

}
