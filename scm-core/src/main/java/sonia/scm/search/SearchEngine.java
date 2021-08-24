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


  ForIndices forIndices();

  interface ForIndices {

    ForIndices withPredicate(Predicate<IndexDetails> predicate);
    ForIndices forResource(String resource);
    default ForIndices forResource(ModelObject resource) {
      return forResource(resource.getId());
    }

    ForIndices withOptions(IndexOptions options);

    void batch(SerializableIndexTask<?> task);
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
     * Specify options for the index.
     * If not used the default options will be used.
     * @param options index options
     * @return {@code this}
     * @see IndexOptions#defaults()
     */
    ForType<T> withOptions(IndexOptions options);

    /**
     * Name of the index which should be used.
     * If not specified the default index will be used.
     * @param name name of index
     * @return {@code this}
     */
    ForType<T> withIndex(String name);

    default ForType<T> forResource(ModelObject resource) {
      return forResource(resource.getId());
    }

    ForType<T> forResource(String id);

    /**
     * TODO
     */
    void update(SerializableIndexTask<T> task);

    /**
     * TODO
     */
    void update(Class<? extends IndexTask<T>> task);

    /**
     * Returns a query builder object which can be used to search the index.
     * @return query builder
     */
    QueryBuilder<T> search();
  }
}
