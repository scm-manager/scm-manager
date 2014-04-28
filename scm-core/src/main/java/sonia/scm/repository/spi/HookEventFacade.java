/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;

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

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   *
   * @throws RepositoryException
   */
  public HookEventHandler handle(String id) throws RepositoryException
  {
    return handle(repositoryManagerProvider.get().get(id));
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param repositoryName
   *
   * @return
   *
   * @throws RepositoryException
   */
  public HookEventHandler handle(String type, String repositoryName)
    throws RepositoryException
  {
    return handle(repositoryManagerProvider.get().get(type, repositoryName));
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   * @throws RepositoryException
   */
  public HookEventHandler handle(Repository repository)
    throws RepositoryException
  {
    if (repository == null)
    {
      throw new RepositoryNotFoundException("could not find repository");
    }

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
