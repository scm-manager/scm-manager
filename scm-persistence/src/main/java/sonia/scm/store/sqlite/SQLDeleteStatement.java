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

class SQLDeleteStatement extends ConditionalSQLStatement {

  private final SQLTable fromTable;

  SQLDeleteStatement(SQLTable fromTable, List<SQLNodeWithValue> whereCondition) {
    super(whereCondition);
    this.fromTable = fromTable;

  }

  @Override
  public String toSQL() {
    StringBuilder query = new StringBuilder();
    query.append("DELETE FROM ").append(fromTable.toSQL());
    appendWhereClause(query);
    return query.toString();
  }
}
