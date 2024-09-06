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
