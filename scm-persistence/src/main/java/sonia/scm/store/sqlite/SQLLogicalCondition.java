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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
class SQLLogicalCondition implements SQLNodeWithValue {
  private String operator; // AND, OR
  private List<SQLNodeWithValue> conditions;

  SQLLogicalCondition(String operator, List<SQLNodeWithValue> conditions) {
    this.operator = operator;
    this.conditions = conditions;
  }

  @Override
  public String toSQL() {
    if (conditions == null || conditions.isEmpty()) {
      return "";
    }

    StringBuilder sql = new StringBuilder();

    if (operator.equals("NOT")) {
      sql.append("NOT ");
    }

    for (int i = 0; i < conditions.size(); i++) {
      if (i > 0) {
        sql.append(" ").append(operator).append(" ");
      }
      String conditionSQL = conditions.get(i).toSQL();
      sql.append("(").append(conditionSQL).append(")");
    }
    return sql.toString();
  }

  @Override
  public int apply(PreparedStatement statement, int index) throws SQLException {
    int currentIndex = index;
    for (SQLNodeWithValue condition : conditions) {
      currentIndex = condition.apply(statement, currentIndex);
    }
    return currentIndex;
  }
}
