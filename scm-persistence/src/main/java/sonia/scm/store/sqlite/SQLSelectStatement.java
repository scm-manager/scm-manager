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

package sonia.scm.store.sqlite;

import java.util.List;
import java.util.stream.Collectors;

class SQLSelectStatement extends ConditionalSQLStatement {

  private final List<SQLField> columns;
  private final SQLTable fromTable;
  private final String orderBy;
  private final long limit;
  private final long offset;

  SQLSelectStatement(List<SQLField> columns, SQLTable fromTable, List<SQLNodeWithValue> whereCondition) {
    this(columns, fromTable, whereCondition, null, 0, 0);
  }

  SQLSelectStatement(List<SQLField> columns, SQLTable fromTable, List<SQLNodeWithValue> whereCondition, String orderBy, long limit, long offset) {
    super(whereCondition);
    if (limit < 0 || offset < 0) {
      throw new IllegalArgumentException("limit and offset must be non-negative");
    }
    this.columns = columns;
    this.fromTable = fromTable;
    this.orderBy = orderBy;
    this.limit = limit;
    this.offset = offset;
  }

  @Override
  public String toSQL() {
    StringBuilder query = new StringBuilder();

    query.append("SELECT ");
    if (columns != null && !columns.isEmpty()) {
      String columnList = columns.stream()
        .map(SQLField::toSQL)
        .collect(Collectors.joining(", "));
      query.append(columnList);
    }
    query.append(" FROM ").append(fromTable.toSQL());

    appendWhereClause(query);

    if (orderBy != null && !orderBy.isEmpty()) {
      query.append(" ORDER BY ").append(orderBy);
    } else {
      query.append(" ORDER BY ROWID");
    }

    if (limit > 0) {
      query.append(" LIMIT ").append(limit);
    }

    if (offset > 0) {
      query.append(" OFFSET ").append(offset);
    }

    return query.toString();
  }
}
