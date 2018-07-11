/**
 * Copyright (c) 2010, Sebastian Sdorra
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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Type;
import sonia.scm.TypeManager;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

//~--- JDK imports ------------------------------------------------------------

/**
 * The central class for managing {@link Repository} objects.
 * This class is a singleton and is available via injection.
 *
 * @author Sebastian Sdorra
 *
 * @apiviz.uses sonia.scm.repository.RepositoryHandler
 */
public interface RepositoryManager
  extends TypeManager<Repository, RepositoryHandler, RepositoryException>
{

  /**
   * Fire {@link RepositoryHookEvent} to the event bus.
   *
   * @param event hook event
   *
   * @since 2.0.0
   */
  public void fireHookEvent(RepositoryHookEvent event);

  /**
   * Imports an existing {@link Repository}.
   * Note: This method should only be called from a {@link RepositoryHandler}.
   *
   *
   * @param repository {@link Repository} to import
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public void importRepository(Repository repository)
    throws IOException, RepositoryException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a {@link Repository} by its namespace and name or
   * null if the {@link Repository} could not be found.
   *
   *
   * @param namespaceAndName namespace and name of the {@link Repository}
   *
   *
   * @return {@link Repository} by its namespace and name or null
   * if the {@link Repository} could not be found
   */
  public Repository get(NamespaceAndName namespaceAndName);

  /**
   * Returns all configured repository types.
   *
   *
   * @return all configured repository types
   */
  public Collection<Type> getConfiguredTypes();

  /**
   * Returns the {@link Repository} associated to the request uri.
   *
   *
   * @param request the current http request
   *
   * @return associated to the request uri
   * @since 1.9
   */
  public Repository getFromRequest(HttpServletRequest request);

  /**
   * Returns the {@link Repository} associated to the request uri.
   *
   *
   *
   * @param uri request uri without context path
   *
   * @return  associated to the request uri
   * @since 1.9
   */
  public Repository getFromUri(String uri);

  /**
   * Returns a {@link RepositoryHandler} by the given type (hg, git, svn ...).
   *
   *
   * @param type the type of the {@link RepositoryHandler}
   *
   * @return {@link RepositoryHandler} by the given type
   */
  @Override
  public RepositoryHandler getHandler(String type);

  default Optional<Repository> getByNamespace(String namespace, String name) {
    return getAll()
      .stream()
      .filter(r -> r.getName().equals(name) && r.getNamespace().equals(namespace))
      .findFirst();
  }
}
