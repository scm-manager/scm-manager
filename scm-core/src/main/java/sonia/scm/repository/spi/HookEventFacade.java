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
    
package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

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
 * @author Sebastian Sdorra
 * @since 1.33
 */
public final class HookEventFacade
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryManagerProvider
   * @param hookContextFactory
   *
   * @since 1.38
   */
  @Inject
  public HookEventFacade(Provider<RepositoryManager> repositoryManagerProvider,
    HookContextFactory hookContextFactory)
  {
    this.repositoryManagerProvider = repositoryManagerProvider;
    this.hookContextFactory = hookContextFactory;
  }

  //~--- methods --------------------------------------------------------------

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

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/07/21
   * @author         Enter your name here...
   */
  public static class HookEventHandler
  {

    /**
     * Constructs ...
     *
     *
     * @param repositoryManager
     * @param hookContextFactory
     * @param repository
     */
    public HookEventHandler(RepositoryManager repositoryManager,
      HookContextFactory hookContextFactory, Repository repository)
    {
      this.repositoryManager = repositoryManager;
      this.hookContextFactory = hookContextFactory;
      this.repository = repository;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param type
     * @param hookContextProvider
     */
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

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final HookContextFactory hookContextFactory;

    /** Field description */
    private final Repository repository;

    /** Field description */
    private final RepositoryManager repositoryManager;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final HookContextFactory hookContextFactory;

  /** Field description */
  private final Provider<RepositoryManager> repositoryManagerProvider;
}
