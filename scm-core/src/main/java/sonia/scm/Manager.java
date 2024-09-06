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

package sonia.scm;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Base interface for all manager classes.
 *
 *
 * @param <T> type of the model object
 */
public interface Manager<T extends ModelObject>
        extends HandlerBase<T>, LastModifiedAware
{


  /**
   * Reloads an object from store and overwrites all changes.
   *
   *
   * @param object to refresh
   *
   * @throws NotFoundException
   */
  void refresh(T object);


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
   */
  Collection<T> getAll();

  /**
   * Returns all object of the store unsorted
   *
   * @param filter to filter the returned objects
   * @since 3.1.0
   * @return all object of the store sorted by the given {@link java.util.Comparator}
   */
  default Collection<T> getAll(Predicate<T> filter) {
    return getAll(filter, null);
  }

  /**
   * Returns all object of the store sorted by the given {@link java.util.Comparator}
   *
   *
   * @param filter to filter the returned objects
   * @param comparator to sort the returned objects (may be null if no sorting is needed)
   * @since 1.4
   * @return all object of the store sorted by the given {@link java.util.Comparator}
   */
  Collection<T> getAll(Predicate<T> filter, Comparator<T> comparator);

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
   * <p>This default implementation reads all items, first, so you might want to adapt this
   * whenever reading is expensive!</p>
   *
   * @param filter to filter returned objects
   * @param comparator to sort the returned objects
   * @param pageNumber the number of the page to be returned (zero based)
   * @param pageSize the size of the pages
   *
   * @since 2.0
   * @return {@link PageResult} with the objects from the store for the requested
   *         page. If the requested page number exceeds the existing pages, an
   *         empty page result is returned.
   */
  default PageResult<T> getPage(Predicate<T> filter, Comparator<T> comparator, int pageNumber, int pageSize) {
    return PageResult.createPage(getAll(filter, comparator), pageNumber, pageSize);
  }

}
