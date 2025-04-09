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
import lombok.Setter;
import sonia.scm.store.LeafCondition;
import sonia.scm.store.Operator;
import sonia.scm.store.QueryableStore;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

/**
 * <b>SQLCondition</b> represents a condition given in an agnostic SQL statement.
 *
 * @since 3.8.0
 */
@Getter
@Setter
class SQLCondition implements SQLNodeWithValue {
  private String operatorPrefix;
  private String operatorPostfix;
  private SQLField field;
  private SQLValue value;

  public SQLCondition(String operator, SQLField field, SQLValue value) {
    this(operator, "", field, value);
  }

  public SQLCondition(String operatorPrefix, String operatorPostfix, SQLField field, SQLValue value) {
    this.operatorPrefix = operatorPrefix;
    this.operatorPostfix = operatorPostfix;
    this.field = field;
    this.value = value;
  }

  public static SQLCondition createConditionWithOperatorAndValue(LeafCondition<?, ?> leafCondition) {
    QueryableStore.QueryField<?, ?> queryField = leafCondition.getField();
    String operatorPrefix = mapOperatorPrefix(leafCondition.getOperator());
    String operatorPostfix = mapOperatorPostfix(leafCondition.getOperator());
    SQLField field = new SQLField(computeSQLField(queryField));
    SQLValue value = determineValueBasedOnOperator(leafCondition);
    if (queryField instanceof QueryableStore.CollectionQueryField<?>) {
      return new ExistsSQLCondition(operatorPrefix, field, value);
    } else if (queryField instanceof QueryableStore.MapQueryField<?>) {
      return new ExistsSQLCondition(operatorPrefix, field, value);
    } else {
      return new SQLCondition(operatorPrefix, operatorPostfix, field, value);
    }
  }

  private static String mapOperatorPrefix(Operator operator) {
    return switch (operator) {
      case EQ -> "=";
      case LESS -> "<";
      case LESS_OR_EQUAL -> "<=";
      case GREATER -> ">";
      case GREATER_OR_EQUAL -> ">=";
      case CONTAINS -> "LIKE '%' ||";
      case NULL -> "IS NULL";
      case IN -> "IN";
      case KEY -> "key =";
      case VALUE -> "value =";
    };
  }

  private static String mapOperatorPostfix(Operator operator) {
    return switch (operator) {
      case CONTAINS -> "|| '%'";
      default -> "";
    };
  }

  private static String computeSQLField(QueryableStore.QueryField<?, ?> queryField) {
    if (queryField instanceof QueryableStore.CollectionQueryField<?>) {
      return "select * from json_each(payload, '$." + queryField.getName() + "') where value ";
    } else if (queryField instanceof QueryableStore.MapQueryField<?>) {
      return "select * from json_each(payload, '$." + queryField.getName() + "') where ";
    } else if (queryField instanceof QueryableStore.InstantQueryField) {
      return "json_extract(payload, '$." + queryField.getName() + "')";
    } else {
      return SQLFieldHelper.computeSQLField(queryField);
    }
  }

  private static SQLValue determineValueBasedOnOperator(LeafCondition<?, ?> leafCondition) {
    Operator operator = leafCondition.getOperator();
    Object value = leafCondition.getValue();

    switch (operator) {
      case NULL:
        return new SQLValue(null);

      case IN:
        if (value instanceof Object[] valueArray) {
          return new SQLValue(valueArray);
        } else {
          throw new IllegalArgumentException("Value for IN operator must be an array.");
        }

      default:
        return new SQLValue(computeParameter(leafCondition));
    }
  }

  private static Object computeParameter(LeafCondition<?, ?> leafCondition) {
    if (leafCondition.getField() instanceof QueryableStore.InstantQueryField) {
      return ((Instant) leafCondition.getValue()).toEpochMilli();
    } else {
      return leafCondition.getValue();
    }
  }

  @Override
  public String toSQL() {
    String fieldSQL = (field != null) ? field.toSQL() : "";
    return fieldSQL + " " + operatorPrefix + " " + value.toSQL() + " " + operatorPostfix + " ";
  }

  @Override
  public int apply(PreparedStatement statement, int index) throws SQLException {
    return value.apply(statement, index);
  }

  private static class ExistsSQLCondition extends SQLCondition {
    public ExistsSQLCondition(String operator, SQLField field, SQLValue value) {
      super(operator, field, value);
    }

    @Override
    public String toSQL() {
      return "exists(" + super.toSQL() + ")";
    }
  }
}
