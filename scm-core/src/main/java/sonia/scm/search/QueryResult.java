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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * Result of a query.
 * @since 2.21.0
 */
@Beta
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class QueryResult extends QueryCountResult {

  /**
   * List of hits found by the query.
   * The list contains only those hits which are starting at start and they are limit by the given amount.
   * @see QueryBuilder
   */
  private final List<Hit> hits;

  /**
   * @deprecated since 3.2.0 in favor of the new constructor with queryType
   */
  @Deprecated(since = "3.2.0")
  public QueryResult(long totalHits, Class<?> type, List<Hit> hits) {
    this(totalHits, type, hits, null);
  }

  public QueryResult(long totalHits, Class<?> type, List<Hit> hits, QueryType queryType) {
    super(type, totalHits, queryType);
    this.hits = Collections.unmodifiableList(hits);
  }
}
