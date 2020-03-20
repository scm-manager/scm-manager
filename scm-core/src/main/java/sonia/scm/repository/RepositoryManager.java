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
    
package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.TypeManager;

import java.io.IOException;
import java.util.Collection;

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
  extends TypeManager<Repository, RepositoryHandler>
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
   */
  public void importRepository(Repository repository) throws IOException;

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
  public Collection<RepositoryType> getConfiguredTypes();

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
}
