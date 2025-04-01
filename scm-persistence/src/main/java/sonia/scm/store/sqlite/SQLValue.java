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

import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Representation of a column or a list of columns within an {@link SQLTable}.
 *
 * @since 3.7.0
 */
@Slf4j
class SQLValue implements SQLNodeWithValue {
  private final Object value;

  SQLValue(Object value) {
    this.value = value;
  }

  @Override
  public String toSQL() {
    if (value == null) {
      return "";
    }

    if (value instanceof Object[] valueArray) {
      return generatePlaceholders(valueArray);
    } else if (value instanceof List<?> valueList) {
      return generatePlaceholders(valueList);
    }

    return "?";
  }

  @Override
  public int apply(PreparedStatement statement, int index) throws SQLException {
      if (value instanceof Object[] valueArray) {
        for (int i = 0; i < valueArray.length; i++) {
          set(index + i, valueArray[i], statement);
        }
        return index + valueArray.length;
      } else if (value instanceof List<?> valueList) {
        for (int i = 0; i < valueList.size(); i++) {
          set(index + i, valueList.get(i), statement);
        }
        return index + valueList.size();
      } else if (value == null) {
        return index;
      } else {
        set(index, value, statement);
        return index + 1;
      }
  }

  private static void set(int index, Object value, PreparedStatement statement) throws SQLException {
    log.trace("set index {} to value '{}'", index, value);
    statement.setObject(index, value);
  }

  private String generatePlaceholders(Object[] valueArray) {
    return generatePlaceholders(valueArray.length);
  }

  private String generatePlaceholders(List<?> valueList) {
    return generatePlaceholders(valueList.size());
  }

  private String generatePlaceholders(int length) {
    StringBuilder placeholdersBuilder = new StringBuilder();
    for (int i = 0; i < length; i++) {
      if (i > 0) {
        placeholdersBuilder.append(", ");
      }
      placeholdersBuilder.append("?");
    }
    return "(" + placeholdersBuilder + ")";
  }
}
