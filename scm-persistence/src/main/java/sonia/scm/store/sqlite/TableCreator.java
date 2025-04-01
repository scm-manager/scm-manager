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
import sonia.scm.plugin.QueryableTypeDescriptor;
import sonia.scm.store.StoreException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import static sonia.scm.store.sqlite.SQLiteIdentifiers.computeColumnIdentifier;
import static sonia.scm.store.sqlite.SQLiteIdentifiers.computeTableName;

@Slf4j
class TableCreator {

  private final Connection connection;

  TableCreator(Connection connection) {
    this.connection = connection;
  }

  void initializeTable(QueryableTypeDescriptor descriptor) {
    log.info("initializing table for {}", descriptor);

    String tableName = computeTableName(descriptor);

    Collection<String> columns = getColumns(tableName);

    if (columns.isEmpty()) {
      createTable(descriptor, tableName);
    } else if (!columns.contains("ID")) {
      log.error("table {} exists but does not contain ID column", tableName);
      throw new StoreException("Table " + tableName + " exists but does not contain ID column");
    } else if (!columns.contains("payload")) {
      log.error("table {} exists but does not contain payload column", tableName);
      throw new StoreException("Table " + tableName + " exists but does not contain payload column");
    } else {
      for (String type : descriptor.getTypes()) {
        String column = computeColumnIdentifier(type);
        if (!columns.contains(column)) {
          log.error("table {} exists but does not contain column {}", tableName, column);
          throw new StoreException("Table " + tableName + " exists but does not contain column " + column);
        }
      }
      if (descriptor.getTypes().length != columns.size() - 2) {
        log.error("table {} exists but has too many columns", tableName);
        throw new StoreException("Table " + tableName + " exists but has too many columns");
      }
    }
  }

  private void createTable(QueryableTypeDescriptor descriptor, String tableName) {
    StringBuilder builder = new StringBuilder("CREATE TABLE ")
      .append(tableName)
      .append(" (");
    for (String type : descriptor.getTypes()) {
      builder.append(computeColumnIdentifier(type)).append(" TEXT NOT NULL, ");
    }
    builder.append("ID TEXT NOT NULL, payload JSONB");
    builder.append(", PRIMARY KEY (");
    for (String type : descriptor.getTypes()) {
      builder.append(computeColumnIdentifier(type)).append(", ");
    }
    builder.append("ID)");
    builder.append(')');
    try {
      log.info("creating table {} for {}", tableName, descriptor);
      log.trace("sql: {}", builder);
      boolean result = connection.createStatement().execute(builder.toString());
      log.trace("created: {}", result);
    } catch (SQLException e) {
      throw new StoreException("Failed to create table for class " + descriptor.getClazz() + ": " + builder, e);
    }
  }

  Collection<String> getColumns(String tableName) {
    log.debug("checking table {}", tableName);
    try {
      ResultSet resultSet = connection.createStatement().executeQuery("PRAGMA table_info(" + tableName + ")");
      Collection<String> columns = new LinkedList<>();
      while (resultSet.next()) {
        columns.add(resultSet.getString("name"));
      }
      resultSet.close();
      return columns;
    } catch (SQLException e) {
      throw new StoreException("Failed to get columns for table " + tableName, e);
    }
  }
}
