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

/**
 * Result of a counting query.
 *
 * @since 2.22.0
 * @see QueryBuilder
 */
@Beta
@Getter
@ToString
@EqualsAndHashCode
public class QueryCountResult {

  /**
   * Searched type of object.
   */
  private final Class<?> type;

  /**
   * Total count of hits, which are matched by the query.
   */
  private final long totalHits;

  /**
   * Type of the created query
   */
  private final QueryType queryType;

  /**
   * @deprecated since 3.2.0 in favor of the new constructor with queryType
   */
  @Deprecated(since = "3.2.0")
  public QueryCountResult(Class<?> type, long totalHits) {
    this(type, totalHits, null);
  }

  public QueryCountResult(Class<?> type, long totalHits, QueryType queryType) {
    this.type = type;
    this.totalHits = totalHits;
    this.queryType = queryType;
  }
}
