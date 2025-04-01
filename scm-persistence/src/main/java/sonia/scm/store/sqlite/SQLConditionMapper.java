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

import sonia.scm.store.Condition;
import sonia.scm.store.LeafCondition;
import sonia.scm.store.LogicalCondition;

import java.util.ArrayList;
import java.util.List;

class SQLConditionMapper {

  /**
   * Maps a LogicalCondition to an SQLLogicalCondition and appends its value to the provided parameters list.
   *
   * @param logicalCondition The condition to map.
   * @return A new SQLCondition object representing the mapped condition.
   */
  static SQLLogicalCondition mapToSQLLogicalCondition(LogicalCondition<?> logicalCondition) {

    List<SQLNodeWithValue> sqlConditions = new ArrayList<>();
    for (Condition<?> condition : logicalCondition.getConditions()) {
      if (condition instanceof LeafCondition) {
        sqlConditions.add(SQLConditionMapper.mapToSQLCondition((LeafCondition<?, ?>) condition));
      } else {
        sqlConditions.add(SQLConditionMapper.mapToSQLLogicalCondition((LogicalCondition<?>) condition));
      }
    }

    return new SQLLogicalCondition(
      logicalCondition.getOperator().toString(),
      sqlConditions
    );
  }

  /**
   * Maps a LeafCondition to an SQLCondition and appends its value to the provided parameters list.
   *
   * @param leafCondition The condition to map.
   * @return A new SQLCondition object representing the mapped condition.
   */
  static SQLCondition mapToSQLCondition(LeafCondition<?, ?> leafCondition) {
    return SQLCondition.createConditionWithOperatorAndValue(leafCondition);
  }
}
