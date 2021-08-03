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

import java.util.Collection;

/**
 * The {@link SearchEngine} is the main entry point for indexing and searching.
 * Note that this is kind of a low level api for indexing.
 * For non expert indexing the {@link IndexQueue} should be used.
 *
 * @see IndexQueue
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

  <T> ForType<T> forType(Class<T> type);

  ForType<Object> forType(String name);

  interface ForType<T> {

    ForType<T> withOptions(IndexOptions options);

    ForType<T> withIndex(String name);

    /** Queue **/
    Index<T> getOrCreate();

    QueryBuilder<T> search();

  }
}
