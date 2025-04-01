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

class SQLInsertStatement implements SQLNodeWithValue {

  private final SQLTable fromTable;
  private final SQLValue values;

  SQLInsertStatement(SQLTable fromTable, SQLValue values) {
    this.fromTable = fromTable;
    this.values = values;
  }

  @Override
  public String toSQL() {
    return "REPLACE INTO " + fromTable.toSQL() + " VALUES " + values.toSQL();
  }

  @Override
  public int apply(PreparedStatement statement, int index) throws SQLException {
    return values.apply(statement, index);
  }

  @Override
  public String toString() {
    return "SQL insert statement: " + toSQL();
  }
}
