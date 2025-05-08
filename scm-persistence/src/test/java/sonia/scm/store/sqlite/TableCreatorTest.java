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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.QueryableTypeDescriptor;
import sonia.scm.store.StoreException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static sonia.scm.store.sqlite.QueryableTypeDescriptorTestData.createDescriptor;

@ExtendWith(MockitoExtension.class)
class TableCreatorTest {

  private final Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");;

  private final TableCreator tableCreator = new TableCreator(connection);

  TableCreatorTest() throws SQLException {
  }

  @Test
  void shouldCreateTableWithoutParents() throws SQLException {
    QueryableTypeDescriptor descriptor = createDescriptor("com.cloudogu.space.to.be.Spaceship", new String[0]);

    tableCreator.initializeTable(descriptor);

    assertThat(getColumns("com_cloudogu_space_to_be_Spaceship_STORE"))
      .containsEntry("ID", "TEXT")
      .containsEntry("payload", "JSONB");
  }

  @Test
  void shouldCreateNamedTableWithoutParents() throws SQLException {
    QueryableTypeDescriptor descriptor = createDescriptor("ships", "com.cloudogu.space.to.be.Spaceship", new String[0]);

    tableCreator.initializeTable(descriptor);

    assertThat(getColumns("ships_STORE"))
      .containsEntry("ID", "TEXT")
      .containsEntry("payload", "JSONB");
  }

  @Test
  void shouldCreateTableWithSingleParent() throws SQLException {
    QueryableTypeDescriptor descriptor = createDescriptor("com.cloudogu.space.to.be.Spaceship", new String[]{"sonia.scm.repo.Repository.class"});

    tableCreator.initializeTable(descriptor);

    assertThat(getColumns("com_cloudogu_space_to_be_Spaceship_STORE"))
      .containsEntry("Repository_ID", "TEXT")
      .containsEntry("ID", "TEXT")
      .containsEntry("payload", "JSONB");
  }

  @Test
  void shouldCreateTableWithMultipleParents() throws SQLException {
    QueryableTypeDescriptor descriptor = createDescriptor("com.cloudogu.space.to.be.Spaceship", new String[]{"sonia.scm.repo.Repository.class", "sonia.scm.user.User"});

    tableCreator.initializeTable(descriptor);

    assertThat(getColumns("com_cloudogu_space_to_be_Spaceship_STORE"))
      .containsEntry("Repository_ID", "TEXT")
      .containsEntry("User_ID", "TEXT")
      .containsEntry("ID", "TEXT")
      .containsEntry("payload", "JSONB");
  }

  @Test
  void shouldFailIfTableExistsWithoutIdColumn() throws SQLException {
    QueryableTypeDescriptor descriptor = createDescriptor("com.cloudogu.space.to.be.Spaceship", new String[0]);
    try {
      connection.createStatement().execute("CREATE TABLE com_cloudogu_space_to_be_Spaceship_STORE (payload JSONB)");
      tableCreator.initializeTable(descriptor);
      fail("exception expected");
    } catch (StoreException e) {
      assertThat(e.getMessage()).contains("does not contain ID column");
    }
  }

  @Test
  void shouldFailIfTableExistsWithoutPayloadColumn() throws SQLException {
    QueryableTypeDescriptor descriptor = createDescriptor("com.cloudogu.space.to.be.Spaceship", new String[0]);
    try {
      connection.createStatement().execute("CREATE TABLE com_cloudogu_space_to_be_Spaceship_STORE (ID TEXT)");
      tableCreator.initializeTable(descriptor);
      fail("exception expected");
    } catch (StoreException e) {
      assertThat(e.getMessage()).contains("does not contain payload column");
    }
  }

  @Test
  void shouldFailIfTableExistsWithoutParentColumn() throws SQLException {
    QueryableTypeDescriptor descriptor = createDescriptor("com.cloudogu.space.to.be.Spaceship", new String[]{"sonia.scm.repo.Repository.class"});
    try {
      connection.createStatement().execute("CREATE TABLE com_cloudogu_space_to_be_Spaceship_STORE (ID TEXT, payload JSONB)");
      tableCreator.initializeTable(descriptor);
      fail("exception expected");
    } catch (StoreException e) {
      assertThat(e.getMessage()).contains("does not contain column Repository_ID");
    }
  }

  @Test
  void shouldFailIfTableExistsWithTooManyParentColumns() throws SQLException {
    QueryableTypeDescriptor descriptor = createDescriptor("com.cloudogu.space.to.be.Spaceship", new String[]{"sonia.scm.repo.Repository.class"});
    try {
      connection.createStatement().execute("CREATE TABLE com_cloudogu_space_to_be_Spaceship_STORE (ID TEXT, Repository_ID, User_ID, payload JSONB)");
      tableCreator.initializeTable(descriptor);
      fail("exception expected");
    } catch (StoreException e) {
      assertThat(e.getMessage()).contains("but has too many columns");
    }
  }

  private Map<String, String> getColumns(String expectedTableName) throws SQLException {
    ResultSet resultSet = connection.createStatement().executeQuery("PRAGMA table_info("+ expectedTableName +")");
    Map<String, String> columns = new LinkedHashMap<>();
    while (resultSet.next()) {
      columns.put(resultSet.getString("name"), resultSet.getString("type"));
    }
    return columns;
  }
}
