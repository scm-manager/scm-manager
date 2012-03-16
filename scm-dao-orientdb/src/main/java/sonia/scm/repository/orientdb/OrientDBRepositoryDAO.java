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



package sonia.scm.repository.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.SCMContextProvider;
import sonia.scm.Type;
import sonia.scm.repository.BlameViewer;
import sonia.scm.repository.ChangesetViewer;
import sonia.scm.repository.DiffViewer;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryBrowser;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryHook;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryListener;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public class OrientDBRepositoryDAO implements RepositoryManager
{

  /**
   * Method description
   *
   *
   * @param hook
   */
  @Override
  public void addHook(RepositoryHook hook)
  {
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param object
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void create(Repository object) throws RepositoryException, IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param object
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void delete(Repository object) throws RepositoryException, IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param object
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void modify(Repository object) throws RepositoryException, IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param object
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void refresh(Repository object) throws RepositoryException, IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public Repository get(String id)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Repository> getAll()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param comparator
   *
   * @return
   */
  @Override
  public Collection<Repository> getAll(Comparator<Repository> comparator)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public Collection<Repository> getAll(int start, int limit)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param comparator
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public Collection<Repository> getAll(Comparator<Repository> comparator,
          int start, int limit)
  {
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Long getLastModified()
  {
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
