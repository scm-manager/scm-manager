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

package sonia.scm.search;

import com.google.common.annotations.Beta;
import sonia.scm.ModelObject;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * The {@link SearchEngine} is the main entry point for indexing and searching.
 *
 * @since 2.21.0
 */
@Beta
public interface SearchEngine {

  /**
   * Returns a list of searchable types.
   *
   * @return collection of searchable types
   */
  Collection<SearchableType> getSearchableTypes();

  /**
   * Returns a type specific api which can be used to index objects of that specific type.
   *
   * @param type type of object
   * @param <T> type of object
   * @return type specific index and search api
   * @since 2.23.0
   */
  <T> ForType<T> forType(Class<T> type);

  /**
   * Returns an api which can be used to index and search object of that type.
   * @param name name of type
   * @return search and index api
   * @since 2.23.0
   */
  ForType<Object> forType(String name);

  /**
   * Returns an api which allows the modification of multiple indices at once.
   * @return api to modify multiple indices
   * @since 2.23.0
   */
  ForIndices forIndices();

  /**
   * Api for modifying multiple indices at once.
   * @since 2.23.0
   */
  interface ForIndices {

    /**
     * This method can be used to filter the indices.
     * If no predicate is used the tasks are enqueued for every existing index.
     *
     * @param predicate predicate to filter indices
     * @return {@code this}
     */
    ForIndices matching(Predicate<IndexDetails> predicate);

    /**
     * Apply a lock for a specific resource. By default, a lock for the whole index is used.
     * If one or more specific resources are locked, than the lock is applied only for those resources
     * and tasks which targets other resources of the same index can run in parallel.
     *
     * @param resource specific resource to lock
     * @return {@code this}
     */
    ForIndices forResource(String resource);

    /**
     * This method is a shortcut for {@link #forResource(String)} with the id of the given resource.
     *
     * @param resource resource in form of model object
     * @return {@code this}
     */
    default ForIndices forResource(ModelObject resource) {
      return forResource(resource.getId());
    }

    /**
     * Submits the task and execute it for every index
     * which are matching the predicate ({@link #matching(Predicate)}.
     * The task is executed asynchronous and will be finished some time in the future.
     * <strong>Note:</strong> the task must be serializable because it is submitted to the
     * {@link sonia.scm.work.CentralWorkQueue}.
     * For more information on task serialization have a look at the
     * {@link sonia.scm.work.CentralWorkQueue} documentation.
     *
     * @param task serializable task for updating multiple indices
     */
    void batch(SerializableIndexTask<?> task);

    /**
     * Submits the task and executes it for every index
     * which are matching the predicate ({@link #matching(Predicate)}.
     * The task is executed asynchronously and will finish at some unknown point in the future.
     *
     * @param task task for updating multiple indices
     */
    void batch(Class<? extends IndexTask<?>> task);
  }

  /**
   * Search and index api.
   *
   * @param <T> type of searchable objects
   * @since 2.23.0
   */
  interface ForType<T> {

    /**
     * Name of the index which should be used.
     * If not specified the default index will be used.
     * @param name name of index
     * @return {@code this}
     */
    ForType<T> withIndex(String name);

    /**
     * Apply a lock for a specific resource. By default, a lock for the whole index is used.
     * If one or more specific resources are locked, then the lock is applied only for those resources
     * and tasks which target other resources of the same index can run in parallel.
     *
     * @param resource specific resource to lock
     * @return {@code this}
     */
    ForType<T> forResource(String resource);

    /**
     * This method is a shortcut for {@link #forResource(String)} with the id of the given resource.
     *
     * @param resource resource in form of model object
     * @return {@code this}
     */
    default ForType<T> forResource(ModelObject resource) {
      return forResource(resource.getId());
    }

    /**
     * Submits a task to update the index.
     * The task is executed asynchronously and will finish at some unknown point in the future.
     * <strong>Note:</strong> the task must be serializable because it is submitted to the
     * {@link sonia.scm.work.CentralWorkQueue},
     * for more information about the task serialization have a look at the
     * {@link sonia.scm.work.CentralWorkQueue} documentation.
     *
     * @param task serializable task for updating the index
     */
    void update(SerializableIndexTask<T> task);

    /**
     * Submits a task to update the index.
     * The task is executed asynchronous and will be finished some time in the future.
     *
     * @param task task for updating multiple indices
     */
    void update(Class<? extends IndexTask<T>> task);

    /**
     * Returns a query builder object which can be used to search the index.
     */
    QueryBuilder<T> search();
  }
}
