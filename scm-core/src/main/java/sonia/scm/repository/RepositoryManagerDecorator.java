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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.ManagerDecorator;
import sonia.scm.Type;
import sonia.scm.plugin.ExtensionPoint;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

/**
 * Decorator for {@link RepositoryManager}.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
@ExtensionPoint
public class RepositoryManagerDecorator
  extends ManagerDecorator<Repository, RepositoryException>
  implements RepositoryManager
{

  /**
   * Constructs ...
   *
   *
   * @param decorated
   */
  public RepositoryManagerDecorator(RepositoryManager decorated)
  {
    super(decorated);
    this.decorated = decorated;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param hook
   */
  @Override
  public void addHook(RepositoryHook hook)
  {
    decorated.addHook(hook);
  }

  /**
   * Method description
   *
   *
   * @param hooks
   */
  @Override
  public void addHooks(Collection<RepositoryHook> hooks)
  {
    decorated.addHooks(hooks);
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void addListener(RepositoryListener listener)
  {
    decorated.addListener(listener);
  }

  /**
   * Method description
   *
   *
   * @param listeners
   */
  @Override
  public void addListeners(Collection<RepositoryListener> listeners)
  {
    decorated.addListeners(listeners);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param event
   */
  @Override
  public void fireHookEvent(Repository repository, RepositoryHookEvent event)
  {
    decorated.fireHookEvent(repository, event);
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   * @param event
   *
   * @throws RepositoryNotFoundException
   */
  @Override
  public void fireHookEvent(String type, String name, RepositoryHookEvent event)
    throws RepositoryNotFoundException
  {
    decorated.fireHookEvent(type, name, event);
  }

  /**
   * Method description
   *
   *
   * @param id
   * @param event
   *
   * @throws RepositoryNotFoundException
   */
  @Override
  public void fireHookEvent(String id, RepositoryHookEvent event)
    throws RepositoryNotFoundException
  {
    decorated.fireHookEvent(id, event);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void importRepository(Repository repository)
    throws IOException, RepositoryException
  {
    decorated.importRepository(repository);
  }

  /**
   * Method description
   *
   *
   * @param hook
   */
  @Override
  public void removeHook(RepositoryHook hook)
  {
    decorated.removeHook(hook);
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(RepositoryListener listener)
  {
    decorated.removeListener(listener);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   *
   * @return
   */
  @Override
  public Repository get(String type, String name)
  {
    return decorated.get(type, name);
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
  @Override
  public BlameViewer getBlameViewer(Repository repository)
    throws RepositoryException
  {
    return decorated.getBlameViewer(repository);
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
  @Override
  public ChangesetViewer getChangesetViewer(Repository repository)
    throws RepositoryException
  {
    return decorated.getChangesetViewer(repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Type> getConfiguredTypes()
  {
    return decorated.getConfiguredTypes();
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
  @Override
  public DiffViewer getDiffViewer(Repository repository)
    throws RepositoryException
  {
    return decorated.getDiffViewer(repository);
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  public Repository getFromRequest(HttpServletRequest request)
  {
    return decorated.getFromRequest(request);
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param uri
   *
   * @return
   */
  @Override
  public Repository getFromTypeAndUri(String type, String uri)
  {
    return decorated.getFromTypeAndUri(type, uri);
  }

  /**
   * Method description
   *
   *
   * @param uri
   *
   * @return
   */
  @Override
  public Repository getFromUri(String uri)
  {
    return decorated.getFromUri(uri);
  }

  /**
   * Method description
   *
   *
   * @param type
   *
   * @return
   */
  @Override
  public RepositoryHandler getHandler(String type)
  {
    return decorated.getHandler(type);
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
  @Override
  public RepositoryBrowser getRepositoryBrowser(Repository repository)
    throws RepositoryException
  {
    return decorated.getRepositoryBrowser(repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Type> getTypes()
  {
    return decorated.getTypes();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryManager decorated;
}
