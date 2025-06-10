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
import java.util.stream.Collectors;

import static java.lang.String.format;

class SQLiteRetainStatement implements SQLNodeWithValue {

  private final SQLTable table;
  private final List<SQLField> columns;
  private final SQLSelectStatement selectStatement;
  private final List<SQLNodeWithValue> parentConditions;


  SQLiteRetainStatement(SQLTable table, List<SQLField> columns, SQLSelectStatement selectStatement, List<SQLNodeWithValue> parentConditions) {
    this.table = table;
    this.columns = columns;
    this.selectStatement = selectStatement;
    this.parentConditions = parentConditions;
  }

  @Override
  public int apply(PreparedStatement statement, int index) throws SQLException {
    index = selectStatement.apply(statement, index);
    for (SQLNodeWithValue condition : parentConditions) {
      index = condition.apply(statement, index);
    }
    return index;
  }

  @Override
  public String toSQL() {
    String parentConditionStatement;
    if (parentConditions == null || parentConditions.isEmpty()) {
      parentConditionStatement = "";
    } else {
      parentConditionStatement = "AND " + new SQLLogicalCondition("AND", parentConditions).toSQL();
    }
    return format("DELETE FROM %s WHERE (%s) NOT IN (%s) %s",
            table.toSQL(),
            columns.stream().map(SQLField::toSQL).collect(Collectors.joining(",")),
            selectStatement.toSQL(),
            parentConditionStatement);
  }
}
