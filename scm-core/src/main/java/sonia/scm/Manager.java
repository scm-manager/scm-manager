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



package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import sonia.scm.util.Util;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;

/**
 * Base interface for all manager classes.
 *
 * @author Sebastian Sdorra
 *
 * @param <T> type of the model object
 * @param <E> type of the exception
 */
public interface Manager<T extends ModelObject, E extends Exception>
        extends HandlerBase<T, E>, LastModifiedAware
{

  /**
   * Reloads a object from store and overwrites all changes.
   *
   *
   * @param object to refresh
   *
   * @throws E
   * @throws IOException
   */
  void refresh(T object) throws E, IOException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the object with the given id.
   *
   *
   * @param id of the object
   *
   * @return object with the given id
   */
  T get(String id);

  /**
   * Returns a {@link java.util.Collection} of all objects in the store.
   *
   *
   * @return all object in the store
   */
  Collection<T> getAll();

  /**
   * Returns all object of the store sorted by the given {@link java.util.Comparator}
   *
   *
   * @param comparator to sort the returned objects
   * @since 1.4
   * @return all object of the store sorted by the given {@link java.util.Comparator}
   */
  Collection<T> getAll(Comparator<T> comparator);

  /**
   * Returns objects from the store which are starts at the given start
   * parameter. The objects returned are limited by the limit parameter.
   *
   *
   * @param start parameter
   * @param limit parameter
   *
   * @since 1.4
   * @return objects from the store which are starts at the given 
   *         start parameter
   */
  Collection<T> getAll(int start, int limit);

  /**
   * Returns objects from the store which are starts at the given start
   * parameter sorted by the given {@link java.util.Comparator}. 
   * The objects returned are limited by the limit parameter.
   *
   *
   * @param comparator to sort the returned objects
   * @param start parameter
   * @param limit parameter
   *
   * @since 1.4
   * @return objects from the store which are starts at the given 
   *         start parameter
   */
  Collection<T> getAll(Comparator<T> comparator, int start, int limit);

  /**
   * Returns objects from the store divided into pages with the given page
   * size for the given page number (zero based) and sorted by the given
   * {@link java.util.Comparator}.
   *
   * @param comparator to sort the returned objects
   * @param pageNumber the number of the page to be returned (zero based)
   * @param pageSize the size of the pages
   *
   * @since 2.0
   * @return {@link PageResult} with the objects from the store for the requested
   *         page. If the requested page number exceeds the existing pages, an
   *         empty page result is returned.
   */
  default PageResult<T> getPage(Comparator<T> comparator, int pageNumber, int pageSize) {
    Collection<T> entities = getAll(comparator, pageNumber * pageSize, pageSize + 1);
    boolean hasMore = entities.size() > pageSize;
    return new PageResult<>(Util.createSubCollection(entities, 0, pageSize), hasMore);
  }

}
