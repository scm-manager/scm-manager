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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.user.User;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("resource")
class SQLiteQueryableMutableStoreTest {

  private Connection connection;
  private String connectionString;

  @BeforeEach
  void init(@TempDir Path path) throws SQLException {
    connectionString = "jdbc:sqlite:" + path.toString() + "/test.db";
    connection = DriverManager.getConnection(connectionString);
  }

  @AfterEach
  void closeDB() throws SQLException {
    connection.close();
  }

  @Nested
  class Put {

    @Test
    void shouldPutObjectWithoutParent() throws SQLException {
      new StoreTestBuilder(connectionString).withIds().put("tricia", new User("trillian"));

      ResultSet resultSet = connection
        .createStatement()
        .executeQuery("SELECT json_extract(u.payload, '$.name') as name FROM sonia_scm_user_User_STORE u WHERE ID = 'tricia'");

      assertThat(resultSet.next()).isTrue();
      assertThat(resultSet.getString("name")).isEqualTo("trillian");
    }

    @Test
    void shouldOverwriteExistingObject() throws SQLException {
      new StoreTestBuilder(connectionString).withIds().put("tricia", new User("Trillian"));
      new StoreTestBuilder(connectionString).withIds().put("tricia", new User("McMillan"));

      ResultSet resultSet = connection
        .createStatement()
        .executeQuery("SELECT json_extract(u.payload, '$.name') as name FROM sonia_scm_user_User_STORE u WHERE ID = 'tricia'");

      assertThat(resultSet.next()).isTrue();
      assertThat(resultSet.getString("name")).isEqualTo("McMillan");
    }

    @Test
    void shouldPutObjectWithSingleParent() throws SQLException {
      new StoreTestBuilder(connectionString, "sonia.Group").withIds("42")
        .put("tricia", new User("trillian"));

      ResultSet resultSet = connection
        .createStatement()
        .executeQuery("SELECT json_extract(u.payload, '$.name') as name FROM sonia_scm_user_User_STORE u WHERE ID = 'tricia' and GROUP_ID = '42'");

      assertThat(resultSet.next()).isTrue();
      assertThat(resultSet.getString("name")).isEqualTo("trillian");
    }

    @Test
    void shouldPutObjectWithMultipleParents() throws SQLException {
      new StoreTestBuilder(connectionString, "sonia.Company", "sonia.Group").withIds("cloudogu", "42")
        .put("tricia", new User("trillian"));

      ResultSet resultSet = connection
        .createStatement()
        .executeQuery("""
                    SELECT json_extract(u.payload, '$.name') as name
                    FROM sonia_scm_user_User_STORE u
                    WHERE ID = 'tricia'
                    AND   GROUP_ID = '42'
                    AND   COMPANY_ID = 'cloudogu'
          """);

      assertThat(resultSet.next()).isTrue();
      assertThat(resultSet.getString("name")).isEqualTo("trillian");
    }

    @Test
    void shouldRollback() throws SQLException {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();

      store.transactional(() -> {
        store.put("tricia", new User("trillian"));
        return false;
      });

      ResultSet resultSet = connection
        .createStatement()
        .executeQuery("SELECT * FROM sonia_scm_user_User_STORE");
      assertThat(resultSet.next()).isFalse();
    }

    @Test
    void shouldDisableAutoCommit() throws SQLException {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();

      store.transactional(() -> {
        store.put("tricia", new User("trillian"));

        try {
          ResultSet resultSet = connection
            .createStatement()
            .executeQuery("SELECT * FROM sonia_scm_user_User_STORE");
          assertThat(resultSet.next()).isFalse();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }

        return true;
      });

      ResultSet resultSet = connection
        .createStatement()
        .executeQuery("SELECT * FROM sonia_scm_user_User_STORE");
      assertThat(resultSet.next()).isTrue();
    }
  }

  @Nested
  class Get {

    @Test
    void shouldGetObjectWithoutParent() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      store.put("tricia", new User("trillian"));

      User tricia = store.get("tricia");

      assertThat(tricia)
        .isNotNull()
        .extracting("name")
        .isEqualTo("trillian");
    }

    @Test
    void shouldReturnForNotExistingValue() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      User earth = store.get("earth");

      assertThat(earth)
        .isNull();
    }

    @Test
    void shouldGetObjectWithSingleParent() {
      new StoreTestBuilder(connectionString, new String[]{"sonia.Group"}).withIds("1337").put("tricia", new User("McMillan"));
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Group").withIds("42");
      store.put("tricia", new User("trillian"));

      User tricia = store.get("tricia");

      assertThat(tricia)
        .isNotNull()
        .extracting("name")
        .isEqualTo("trillian");
    }

    @Test
    void shouldGetObjectWithMultipleParents() {
      new StoreTestBuilder(connectionString, new String[]{"sonia.Company", "sonia.Group"}).withIds("cloudogu", "1337").put("tricia", new User("McMillan"));
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Company", "sonia.Group").withIds("cloudogu", "42");
      store.put("tricia", new User("trillian"));

      User tricia = store.get("tricia");

      assertThat(tricia)
        .isNotNull()
        .extracting("name")
        .isEqualTo("trillian");
    }

    @Test
    void shouldGetAllForSingleEntry() {
      new StoreTestBuilder(connectionString, new String[]{"sonia.Company", "sonia.Group"}).withIds("cloudogu", "1337").put("tricia", new User("McMillan"));
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Company", "sonia.Group").withIds("cloudogu", "42");
      store.put("tricia", new User("trillian"));

      Map<String, User> users = store.getAll();

      assertThat(users).hasSize(1);
      assertThat(users.get("tricia"))
        .isNotNull()
        .extracting("name")
        .isEqualTo("trillian");
    }

    @Test
    void shouldGetAllForMultipleEntries() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Company", "sonia.Group").withIds("cloudogu", "42");
      store.put("dent", new User("arthur"));
      store.put("tricia", new User("trillian"));

      Map<String, User> users = store.getAll();

      assertThat(users).hasSize(2);
      assertThat(users.get("tricia"))
        .isNotNull()
        .extracting("name")
        .isEqualTo("trillian");
      assertThat(users.get("dent"))
        .isNotNull()
        .extracting("name")
        .isEqualTo("arthur");
    }
  }

  @Nested
  class WithAnnotatedId {

    @Test
    void shouldUseIdFromItemOnPut() {
      SQLiteQueryableMutableStore<SpaceshipWithId> store = new StoreTestBuilder(connectionString).forClassWithIds(SpaceshipWithId.class);

      String id = store.put(new SpaceshipWithId("Heart of Gold", 42));
      SpaceshipWithId spaceship = store.get("Heart of Gold");

      assertThat(id).isEqualTo("Heart of Gold");
      assertThat(spaceship).isNotNull();
    }

    @Test
    void shouldSetNewIdInItemOnPut() {
      SQLiteQueryableMutableStore<SpaceshipWithId> store = new StoreTestBuilder(connectionString).forClassWithIds(SpaceshipWithId.class);

      String id = store.put(new SpaceshipWithId());
      SpaceshipWithId spaceship = store.get(id);

      assertThat(spaceship.getName()).isNotNull();
      assertThat(spaceship.getName()).isEqualTo(id);
    }

    @Test
    void shouldStoreWithNewIdAfterManualChange() {
      SQLiteQueryableMutableStore<SpaceshipWithId> store = new StoreTestBuilder(connectionString).forClassWithIds(SpaceshipWithId.class);

      store.put(new SpaceshipWithId("Heart of Gold", 42));
      SpaceshipWithId spaceship = store.get("Heart of Gold");

      store.put("Space Zeppelin", spaceship);

      SpaceshipWithId zeppelin = store.get("Space Zeppelin");
      assertThat(zeppelin.getName()).isEqualTo("Space Zeppelin");

      spaceship = store.get("Heart of Gold");
      assertThat(spaceship.getName()).isEqualTo("Heart of Gold");
    }
  }

  @Nested
  class Clear {
    @Test
    void shouldClear() {
      SQLiteQueryableMutableStore<User> uneffectedStore = new StoreTestBuilder(connectionString, "sonia.Company", "sonia.Group").withIds("cloudogu", "1337");
      uneffectedStore.put("tricia", new User("McMillan"));
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Company", "sonia.Group").withIds("cloudogu", "42");
      store.put("tricia", new User("trillian"));

      store.clear();

      assertThat(store.getAll()).isEmpty();
      assertThat(uneffectedStore.getAll()).hasSize(1);
    }
  }

  @Nested
  class Remove {
    @Test
    void shouldRemove() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Company", "sonia.Group").withIds("cloudogu", "42");
      store.put("dent", new User("arthur"));
      store.put("tricia", new User("trillian"));

      store.remove("dent");

      assertThat(store.getAll()).containsOnlyKeys("tricia");
    }
  }
}
