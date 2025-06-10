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
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStore;
import sonia.scm.user.User;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

  @Nested
  class DeleteAll {
    @Test
    void shouldDeleteAllInStoreWithoutSubsequentQuery() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
      store.put("1", new Spaceship("1"));
      store.put("2", new Spaceship("2"));
      store.put("3", new Spaceship("3"));

      store.query()
        .orderBy(Spaceship.SPACESHIP_NAME, QueryableStore.Order.ASC)
        .deleteAll();

      assertThat(store.getAll()).isEmpty();
    }

    @Test
    void shouldOnlyDeleteElementsMatchingTheQuery() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
      store.put("1", new Spaceship("1"));
      store.put("2", new Spaceship("2"));
      store.put("3", new Spaceship("3"));

      store.query(Spaceship.SPACESHIP_ID.in("1", "3"))
        .orderBy(Spaceship.SPACESHIP_NAME, QueryableStore.Order.ASC)
        .deleteAll();

      assertThat(store.getAll()).hasSize(1);
      assertThat(store.getAll()).containsOnlyKeys("2");
    }

    @Test
    void shouldKeepEntriesFromOtherStore() {
      StoreTestBuilder spaceshipStoreBuilder = new StoreTestBuilder(connectionString);
      StoreTestBuilder crewmateStoreBuilder = new StoreTestBuilder(connectionString, "Spaceship");
      try (
        SQLiteQueryableMutableStore<Spaceship> spaceshipStore = spaceshipStoreBuilder.forClassWithIds(Spaceship.class);
        SQLiteQueryableMutableStore<Crewmate> crewmateStoreForShipOne = crewmateStoreBuilder.forClassWithIds(Crewmate.class, "1");
        SQLiteQueryableMutableStore<Crewmate> crewmateStoreForShipTwo = crewmateStoreBuilder.forClassWithIds(Crewmate.class, "2")
      ) {
        Spaceship spaceshipOne = new Spaceship("1");
        Spaceship spaceshipTwo = new Spaceship("2");
        spaceshipStore.put(spaceshipOne);
        spaceshipStore.put(spaceshipTwo);

        crewmateStoreForShipOne.put("1", new Crewmate(spaceshipOne));
        crewmateStoreForShipOne.put("2", new Crewmate(spaceshipOne));
        crewmateStoreForShipTwo.put("1", new Crewmate(spaceshipTwo));

        crewmateStoreForShipOne.query().deleteAll();

        assertThat(crewmateStoreForShipOne.getAll()).isEmpty();
        assertThat(crewmateStoreForShipTwo.getAll()).hasSize(1);
      }
    }
  }

  @Nested
  class Retain {
    @Test
    void shouldRetainOneWithAscendingOrder() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
      store.put("1", new Spaceship("1"));
      store.put("2", new Spaceship("2"));
      store.put("3", new Spaceship("3"));

      store.query()
        .orderBy(Spaceship.SPACESHIP_NAME, QueryableStore.Order.ASC)
        .retain(1);

      assertThat(store.getAll()).hasSize(1);
      assertThat(store.get("1")).isNotNull();
    }

    @Test
    void shouldThrowIllegalArgumentExceptionIfKeptElementsIsNegative() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
      store.put("1", new Spaceship("1"));
      store.put("2", new Spaceship("2"));
      store.put("3", new Spaceship("3"));

      QueryableMutableStore.MutableQuery mutableQuery = store.query()
        .orderBy(Spaceship.SPACESHIP_NAME, QueryableStore.Order.ASC);

      assertThatThrownBy(() -> mutableQuery.retain(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRetainOneWithDescendingOrder() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
      store.put("1", new Spaceship("1"));
      store.put("2", new Spaceship("2"));
      store.put("3", new Spaceship("3"));

      store.query()
        .orderBy(Spaceship.SPACESHIP_NAME, QueryableStore.Order.DESC)
        .retain(1);

      assertThat(store.getAll()).hasSize(1);
      assertThat(store.get("3")).isNotNull();
    }

    @Test
    void shouldDeleteUnselectedEntitiesAndRetainKeptElementsFromTheSelectedOnes() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);

      Spaceship spaceshipOne = new Spaceship("LazyShip");
      Spaceship spaceshipTwo = new Spaceship("Biblical Ship");
      Spaceship spaceshipThree = new Spaceship("Millennium");

      spaceshipOne.crew = List.of("Foxtrot", "Icebear", "Possum");
      spaceshipTwo.crew = List.of("Adam", "Eva", "Gabriel", "Lilith", "Michael");
      spaceshipThree.crew = List.of("Chewbacca", "R2-D2", "C3PO", "Han Solo", "Luke Skywalker", "Obi-Wan Kenobi");

      store.put("LazyShip", spaceshipOne);
      store.put("Biblical Ship", spaceshipTwo);
      store.put("Millennium", spaceshipThree);

      store.query(Spaceship.SPACESHIP_CREW_SIZE.greater(3L))
        .orderBy(Spaceship.SPACESHIP_CREW_SIZE, QueryableStore.Order.DESC)
        .retain(1);

      assertThat(store.getAll()).hasSize(1);
      assertThat(store.get("LazyShip")).isNull();
      assertThat(store.get("Biblical Ship")).isNull();
      assertThat(store.get("Millennium")).isNotNull();
    }

    @Test
    void shouldRetainEverythingIfKeptElementsHigherThanContentQuantity() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
      store.put("1", new Spaceship("1"));
      store.put("2", new Spaceship("2"));
      store.put("3", new Spaceship("3"));

      store.query()
        .orderBy(Spaceship.SPACESHIP_NAME, QueryableStore.Order.DESC)
        .retain(5);

      assertThat(store.getAll()).hasSize(3);
    }

    @Test
    void shouldKeepEntriesFromOtherStore() {
      StoreTestBuilder spaceshipStoreBuilder = new StoreTestBuilder(connectionString);
      StoreTestBuilder crewmateStoreBuilder = new StoreTestBuilder(connectionString, "Spaceship");
      try (
        SQLiteQueryableMutableStore<Spaceship> spaceshipStore = spaceshipStoreBuilder.forClassWithIds(Spaceship.class);
        SQLiteQueryableMutableStore<Crewmate> crewmateStoreForShipOne = crewmateStoreBuilder.forClassWithIds(Crewmate.class, "1");
        SQLiteQueryableMutableStore<Crewmate> crewmateStoreForShipTwo = crewmateStoreBuilder.forClassWithIds(Crewmate.class, "2")
      ) {
        Spaceship spaceshipOne = new Spaceship("1");
        Spaceship spaceshipTwo = new Spaceship("2");
        spaceshipStore.put(spaceshipOne);
        spaceshipStore.put(spaceshipTwo);

        crewmateStoreForShipOne.put("1", new Crewmate(spaceshipOne));
        crewmateStoreForShipOne.put("2", new Crewmate(spaceshipOne));
        crewmateStoreForShipTwo.put("1", new Crewmate(spaceshipTwo));
        crewmateStoreForShipTwo.put("2", new Crewmate(spaceshipTwo));

        crewmateStoreForShipOne.query().retain(1);

        assertThat(crewmateStoreForShipOne.getAll()).hasSize(1);
        assertThat(crewmateStoreForShipTwo.getAll()).hasSize(2);
      }
    }

  }
}
