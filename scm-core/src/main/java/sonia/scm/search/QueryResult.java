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
