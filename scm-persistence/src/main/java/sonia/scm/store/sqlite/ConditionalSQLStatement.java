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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

abstract class ConditionalSQLStatement implements SQLNodeWithValue {

  private final List<SQLNodeWithValue> whereCondition;

  ConditionalSQLStatement(List<SQLNodeWithValue> whereCondition) {
    this.whereCondition = whereCondition;
  }

  void appendWhereClause(StringBuilder query) {
    if (!whereCondition.isEmpty()) {
      query.append(" WHERE ").append(new SQLLogicalCondition("AND", whereCondition).toSQL());
    }
  }

  @Override
  public int apply(PreparedStatement statement, int index) throws SQLException {
    for (SQLNodeWithValue condition : whereCondition) {
      index = condition.apply(statement, index);
    }
    return index;
  }

  @Override
  public String toString() {
    return "SQL statement: " + toSQL();
  }
}
