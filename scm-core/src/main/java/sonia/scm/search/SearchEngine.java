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

/**
 * The {@link SearchEngine} is the main entry point for indexing and searching.
 * Note that this is kind of a low level api for indexing.
 * For non expert indexing the {@link IndexQueue} should be used.
 *
 * @see IndexQueue
 * @since 2.21.0
 */
public interface SearchEngine {

  /**
   * Returns the index with the given name and the given options.
   * The index is created if it does not exist.
   * Warning: Be careful, because an index can't be opened multiple times in parallel.
   * If you are not sure how you should index your objects, use the {@link IndexQueue}.
   *
   * @param name    name of the index
   * @param options index options
   * @return existing index or a new one if none exists
   */
  Index getOrCreate(String name, IndexOptions options);

  /**
   * Same as {@link #getOrCreate(String, IndexOptions)} with default options.
   *
   * @param name name of the index
   * @return existing index or a new one if none exists
   * @see IndexOptions#defaults()
   */
  default Index getOrCreate(String name) {
    return getOrCreate(name, IndexOptions.defaults());
  }

  /**
   * Search the index.
   * Returns a {@link QueryBuilder} which allows to query the index.
   *
   * @param name    name of the index
   * @param options options for searching the index
   * @return query builder
   */
  QueryBuilder search(String name, IndexOptions options);

  /**
   * Same as {@link #search(String, IndexOptions)} with default options.
   *
   * @param name name of the index
   * @return query builder
   * @see IndexOptions#defaults()
   */
  default QueryBuilder search(String name) {
    return search(name, IndexOptions.defaults());
  }
}
