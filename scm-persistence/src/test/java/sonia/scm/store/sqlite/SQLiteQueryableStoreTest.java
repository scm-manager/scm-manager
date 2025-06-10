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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.group.Group;
import sonia.scm.repository.Repository;
import sonia.scm.store.Conditions;
import sonia.scm.store.LeafCondition;
import sonia.scm.store.Operator;
import sonia.scm.store.QueryableMaintenanceStore;
import sonia.scm.store.QueryableMaintenanceStore.MaintenanceIterator;
import sonia.scm.store.QueryableMaintenanceStore.MaintenanceStoreEntry;
import sonia.scm.store.QueryableStore;
import sonia.scm.user.User;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static sonia.scm.store.sqlite.Spaceship.SPACESHIP_CREW;
import static sonia.scm.store.sqlite.Spaceship.SPACESHIP_CREW_SIZE;
import static sonia.scm.store.sqlite.Spaceship.SPACESHIP_DESTINATIONS;
import static sonia.scm.store.sqlite.Spaceship.SPACESHIP_DESTINATIONS_SIZE;
import static sonia.scm.store.sqlite.Spaceship.SPACESHIP_FLIGHT_COUNT;
import static sonia.scm.store.sqlite.Spaceship.SPACESHIP_ID;
import static sonia.scm.store.sqlite.Spaceship.SPACESHIP_INSERVICE;
import static sonia.scm.store.sqlite.Spaceship.SPACESHIP_NAME;
import static sonia.scm.store.sqlite.Spaceship.SPACESHIP_RANGE;

@SuppressWarnings({"resource", "unchecked"})
class SQLiteQueryableStoreTest {

  private String connectionString;

  @BeforeEach
  void init(@TempDir Path path) {
    connectionString = "jdbc:sqlite:" + path.toString() + "/test.db";
  }

  @Nested
  class FindAll {

    @Nested
    class QueryClassTypes {

      @Test
      void shouldWorkWithEnums() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        store.put(new Spaceship("Space Shuttle", Range.SOLAR_SYSTEM));
        store.put(new Spaceship("Heart Of Gold", Range.INTER_GALACTIC));

        List<Spaceship> all = store
          .query(SPACESHIP_RANGE.eq(Range.SOLAR_SYSTEM))
          .findAll();

        assertThat(all).hasSize(1);
      }


      @Test
      void shouldWorkWithLongs() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        User trillian = new User("trillian", "McMillan", "tricia@hog.org");
        trillian.setCreationDate(10000000000L);
        store.put(trillian);
        User arthur = new User("arthur", "Dent", "arthur@hog.org");
        arthur.setCreationDate(9999999999L);
        store.put(arthur);

        List<User> all = store.query(
            CREATION_DATE.lessOrEquals(9999999999L)
          )
          .findAll();

        assertThat(all)
          .extracting("name")
          .containsExactly("arthur");
      }

      @Test
      void shouldWorkWithIntegers() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        User trillian = new User("trillian", "McMillan", "tricia@hog.org");
        trillian.setCreationDate(42L);
        store.put(trillian);
        User arthur = new User("arthur", "Dent", "arthur@hog.org");
        arthur.setCreationDate(23L);
        store.put(arthur);

        List<User> all = store.query(
            CREATION_DATE_AS_INTEGER.less(40)
          )
          .findAll();

        assertThat(all)
          .extracting("name")
          .containsExactly("arthur");
      }

      @Test
      void shouldWorkWithNumberCollection() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        User trillian = new User("trillian", "McMillan", "tricia@hog.org");
        trillian.setActive(true);
        store.put(trillian);
        User arthur = new User("arthur", "Dent", "arthur@hog.org");
        arthur.setActive(false);
        store.put(arthur);

        List<User> all = store.query(
            ACTIVE.isTrue()
          )
          .findAll();

        assertThat(all)
          .extracting("name")
          .containsExactly("trillian");
      }

      @Test
      void shouldCountAndWorkWithNumberCollection() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        User trillian = new User("trillian", "McMillan", "tricia@hog.org");
        trillian.setActive(true);
        store.put(trillian);
        User arthur = new User("arthur", "Dent", "arthur@hog.org");
        arthur.setActive(false);
        store.put(arthur);

        long count = store.query(
            ACTIVE.isTrue()
          )
          .count();

        assertThat(count).isEqualTo(1);

      }
    }

    @Nested
    class QueryFeatures {

      @Test
      void shouldHandleCollections() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        store.put(new Spaceship("Spaceshuttle", "Buzz", "Anndre"));
        store.put(new Spaceship("Heart Of Gold", "Trillian", "Arthur", "Ford", "Zaphod", "Marvin"));

        List<Spaceship> result = store.query(
          SPACESHIP_CREW.contains("Marvin")
        ).findAll();

        assertThat(result).hasSize(1);
      }

      @Test
      void shouldCountAndHandleCollections() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        store.put(new Spaceship("Spaceshuttle", "Buzz", "Anndre"));
        store.put(new Spaceship("Heart Of Gold", "Trillian", "Arthur", "Ford", "Zaphod", "Marvin"));

        long result = store.query(
          SPACESHIP_CREW.contains("Marvin")
        ).count();

        assertThat(result).isEqualTo(1);
      }

      @Test
      void shouldCountWithoutConditions() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        store.put(new Spaceship("Spaceshuttle", "Buzz", "Anndre"));
        store.put(new Spaceship("Heart Of Gold", "Trillian", "Arthur", "Ford", "Zaphod", "Marvin"));

        long result = store.query().count();

        assertThat(result).isEqualTo(2);
      }

      @Test
      void shouldHandleEmptyCollectionWithMaxString() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        Integer result = store.query().max(
          SPACESHIP_FLIGHT_COUNT
        );

        assertThat(result).isNull();
      }

      @Nested
      class ForAggregations {

        SQLiteQueryableMutableStore<Spaceship> store;

        @BeforeEach
        void createData() {
          store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
          Spaceship spaceshuttle = new Spaceship("Spaceshuttle", "Buzz", "Anndre");
          spaceshuttle.setFlightCount(12);
          store.put("Spaceshuttle", spaceshuttle);
          Spaceship heartOfGold = new Spaceship("Heart Of Gold", "Trillian", "Arthur", "Ford", "Zaphod", "Marvin");
          heartOfGold.setFlightCount(42);
          store.put("Heart Of Gold", heartOfGold);
          Spaceship vogon = new Spaceship("Vogon", "Prostetnic Vogon Jeltz");
          vogon.setFlightCount(321);
          store.put("Vogon", vogon);
        }

        @Test
        void shouldGetMaxString() {
          String result = store.query().max(
            SPACESHIP_NAME
          );

          assertThat(result).isEqualTo("Vogon");
        }

        @Test
        void shouldGetMaxOfCollectionSize() {
          Long result = store.query().max(
            SPACESHIP_CREW_SIZE
          );

          assertThat(result).isEqualTo(5);
        }

        @Test
        void shouldGetMinOfId() {
          String result = store.query().min(
            SPACESHIP_ID
          );

          assertThat(result).isEqualTo("Heart Of Gold");
        }

        @Test
        void shouldGetMinNumber() {
          int result = store.query().min(
            SPACESHIP_FLIGHT_COUNT
          );

          assertThat(result).isEqualTo(12);
        }

        @Test
        void shouldGetAverageNumber() {
          double result = store.query().average(
            SPACESHIP_FLIGHT_COUNT
          );

          assertThat(result).isEqualTo(125);
        }

        @Test
        void shouldGetSum() {
          int result = store.query().sum(
            SPACESHIP_FLIGHT_COUNT
          );

          assertThat(result).isEqualTo(375);
        }
      }

      @Test
      void shouldHandleCollectionSize() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        store.put(new Spaceship("Spaceshuttle", "Buzz", "Anndre"));
        store.put(new Spaceship("Heart of Gold", "Trillian", "Arthur", "Ford", "Zaphod", "Marvin"));
        store.put(new Spaceship("MillenniumFalcon"));

        List<Spaceship> onlyEmpty = store.query(
          SPACESHIP_CREW_SIZE.isEmpty()
        ).findAll();
        assertThat(onlyEmpty).hasSize(1);
        assertThat(onlyEmpty.get(0).getName()).isEqualTo("MillenniumFalcon");


        List<Spaceship> exactlyTwoCrewMates = store.query(
          SPACESHIP_CREW_SIZE.eq(2L)
        ).findAll();
        assertThat(exactlyTwoCrewMates).hasSize(1);
        assertThat(exactlyTwoCrewMates.get(0).getName()).isEqualTo("Spaceshuttle");

        List<Spaceship> moreThanTwoCrewMates = store.query(
          SPACESHIP_CREW_SIZE.greater(2L)
        ).findAll();
        assertThat(moreThanTwoCrewMates).hasSize(1);
        assertThat(moreThanTwoCrewMates.get(0).getName()).isEqualTo("Heart of Gold");
      }

      @Test
      void shouldHandleMap() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        store.put(new Spaceship("Spaceshuttle", Map.of("moon", true, "earth", true)));
        store.put(new Spaceship("Heart of Gold", Map.of("vogon", true, "earth", true)));
        store.put(new Spaceship("MillenniumFalcon", Map.of("dagobah", false)));

        List<Spaceship> keyResult = store.query(
          SPACESHIP_DESTINATIONS.containsKey("vogon")
        ).findAll();
        assertThat(keyResult).hasSize(1);
        assertThat(keyResult.get(0).getName()).isEqualTo("Heart of Gold");

        List<Spaceship> valueResult = store.query(
          SPACESHIP_DESTINATIONS.containsValue(false)
        ).findAll();
        assertThat(valueResult).hasSize(1);
        assertThat(valueResult.get(0).getName()).isEqualTo("MillenniumFalcon");
      }

      @Test
      void shouldCountAndHandleMap() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        store.put(new Spaceship("Spaceshuttle", Map.of("moon", true, "earth", true)));
        store.put(new Spaceship("Heart of Gold", Map.of("vogon", true, "earth", true)));
        store.put(new Spaceship("MillenniumFalcon", Map.of("dagobah", false)));

        long keyResult = store.query(
          SPACESHIP_DESTINATIONS.containsKey("vogon")
        ).count();

        assertThat(keyResult).isEqualTo(1);


        long valueResult = store.query(
          SPACESHIP_DESTINATIONS.containsValue(false)
        ).count();
        assertThat(valueResult).isEqualTo(1);
      }


      @Test
      void shouldHandleMapSize() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        store.put(new Spaceship("Spaceshuttle", Map.of("moon", true, "earth", true)));
        store.put(new Spaceship("Heart of Gold", Map.of("vogon", true, "earth", true, "dagobah", true)));
        store.put(new Spaceship("MillenniumFalcon", Map.of()));

        List<Spaceship> onlyEmpty = store.query(
          SPACESHIP_DESTINATIONS_SIZE.isEmpty()
        ).findAll();
        assertThat(onlyEmpty).hasSize(1);
        assertThat(onlyEmpty.get(0).getName()).isEqualTo("MillenniumFalcon");


        List<Spaceship> exactlyTwoDestinations = store.query(
          SPACESHIP_DESTINATIONS_SIZE.eq(2L)
        ).findAll();
        assertThat(exactlyTwoDestinations).hasSize(1);
        assertThat(exactlyTwoDestinations.get(0).getName()).isEqualTo("Spaceshuttle");

        List<Spaceship> moreThanTwoDestinations = store.query(
          SPACESHIP_DESTINATIONS_SIZE.greater(2L)
        ).findAll();
        assertThat(moreThanTwoDestinations).hasSize(1);
        assertThat(moreThanTwoDestinations.get(0).getName()).isEqualTo("Heart of Gold");
      }

      @Test
      void shouldRetrieveTime() {
        SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
        Spaceship spaceshuttle = new Spaceship("Spaceshuttle", Range.SOLAR_SYSTEM);
        spaceshuttle.setInServiceSince(Instant.parse("1981-04-12T10:00:00Z"));
        store.put(spaceshuttle);

        Spaceship falcon = new Spaceship("Falcon9", Range.SOLAR_SYSTEM);
        falcon.setInServiceSince(Instant.parse("2015-12-21T10:00:00Z"));
        store.put(falcon);

        List<Spaceship> resultEqOperator = store.query(
          SPACESHIP_INSERVICE.eq(Instant.parse("2015-12-21T10:00:00Z"))).findAll();
        assertThat(resultEqOperator).hasSize(1);
        assertThat(resultEqOperator.get(0).getName()).isEqualTo("Falcon9");

        List<Spaceship> resultBeforeOperator = store.query(
          SPACESHIP_INSERVICE.before(Instant.parse("2000-12-21T10:00:00Z"))).findAll();
        assertThat(resultBeforeOperator).hasSize(1);
        assertThat(resultBeforeOperator.get(0).getName()).isEqualTo("Spaceshuttle");

        List<Spaceship> resultAfterOperator = store.query(
          SPACESHIP_INSERVICE.after(Instant.parse("2000-01-01T00:00:00Z"))).findAll();
        assertThat(resultAfterOperator).hasSize(1);
        assertThat(resultAfterOperator.get(0).getName()).isEqualTo("Falcon9");

        List<Spaceship> resultBetweenOperator = store.query(
          SPACESHIP_INSERVICE.between(Instant.parse("1980-04-12T10:00:00Z"), Instant.parse("2016-12-21T10:00:00Z"))).findAll();
        assertThat(resultBetweenOperator).hasSize(2);

      }

      @Test
      void shouldLimitQuery() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put(new User("trillian", "McMillan", "tricia@hog.org"));
        store.put(new User("arthur", "Dent", "arthur@hog.org"));
        store.put(new User("zaphod", "Beeblebrox", "zaphod@hog.org"));
        store.put(new User("marvin", "Marvin", "marvin@hog.org"));

        List<User> all = store.query()
          .findAll(1, 2);

        assertThat(all)
          .extracting("name")
          .containsExactly("arthur", "zaphod");
      }

      @Test
      void shouldOrderResults() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put(new User("trillian", "McMillan", "tricia@hog.org"));
        store.put(new User("arthur", "Dent", "arthur@hog.org"));
        store.put(new User("zaphod", "Beeblebrox Head 1", "zaphod1@hog.org"));
        store.put(new User("zaphod", "Beeblebrox Head 2", "zaphod2@hog.org"));
        store.put(new User("marvin", "Marvin", "marvin@hog.org"));

        List<User> all = store.query()
          .orderBy(USER_NAME, QueryableStore.Order.ASC)
          .orderBy(DISPLAY_NAME, QueryableStore.Order.DESC)
          .findAll();

        assertThat(all)
          .extracting("mail")
          .containsExactly("arthur@hog.org", "marvin@hog.org", "tricia@hog.org", "zaphod2@hog.org", "zaphod1@hog.org");
      }
    }

    @Nested
    class QueryLogicalHandling {
      @Test
      void shouldQueryForId() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put("1", new User("trillian", "Tricia", "tricia@hog.org"));
        store.put("2", new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
        store.put("3", new User("arthur", "Arthur Dent", "arthur@hog.org"));

        List<User> all = store.query(
            ID.eq("1")
          )
          .findAll();

        assertThat(all)
          .extracting("displayName")
          .containsExactly("Tricia");
      }

      @Test
      void shouldQueryForIdAndOrderByDESC() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put("1", new User("trish", "Tricia", "tricia@hog.org"));
        store.put("2", new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
        store.put("3", new User("arthur", "Arthur Dent", "arthur@hog.org"));

        List<User> all = store.query().orderBy(ID, QueryableStore.Order.DESC)
          .findAll();

        assertThat(all)
          .extracting("name")
          .containsExactly("arthur","trillian","trish");
      }

      @Test
      void shouldOrderIdsAndPayload() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put("1", new User("trish", "Tricia", "tricia@hog.org"));
        store.put("2", new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
        store.put("3", new User("trillian", "Arthur Dent", "arthur@hog.org"));

        List<User> all = store.query()
          .orderBy(USER_NAME, QueryableStore.Order.DESC)
          .orderBy(ID, QueryableStore.Order.ASC)
          .findAll();

        System.out.println(all);
        assertThat(all)
          .extracting("displayName")
          .containsExactly("Tricia","Trillian McMillan","Arthur Dent");
      }

      @Test
      void shouldQueryForParents() {
        new StoreTestBuilder(connectionString, Group.class.getName())
          .withIds("42")
          .put("tricia", new User("trillian", "Tricia", "tricia@hog.org"));
        new StoreTestBuilder(connectionString, Group.class.getName())
          .withIds("1337")
          .put("tricia", new User("trillian", "Trillian McMillan", "tricia@hog.org"));

        SQLiteQueryableStore<User> store = new StoreTestBuilder(connectionString, Group.class.getName()).withIds();

        List<User> all = store.query(
            GROUP.eq("42")
          )
          .findAll();

        assertThat(all)
          .extracting("displayName")
          .containsExactly("Tricia");
      }

      @Test
      void shouldHandleContainsCondition() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put("tricia", new User("trillian", "Tricia", "tricia@hog.org"));
        store.put("McMillan", new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
        store.put("dent", new User("arthur", "Arthur Dent", "arthur@hog.org"));

        List<User> all = store.query(
            USER_NAME.contains("ri")
          )
          .findAll();

        assertThat(all).hasSize(2);
      }

      @Test
      void shouldHandleIsNullCondition() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put("tricia", new User("trillian", null, "tricia@hog.org"));
        store.put("dent", new User("arthur", "Arthur Dent", "arthur@hog.org"));

        List<User> all = store.query(
            DISPLAY_NAME.isNull()
          )
          .findAll();

        assertThat(all)
          .extracting("name")
          .containsExactly("trillian");
      }

      @Test
      void shouldHandleNotNullCondition() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put("tricia", new User("trillian", null, "tricia@hog.org"));
        store.put("dent", new User("arthur", "Arthur Dent", "arthur@hog.org"));

        List<User> all = store.query(
            Conditions.not(DISPLAY_NAME.isNull())
          )
          .findAll();

        assertThat(all)
          .extracting("name")
          .containsExactly("arthur");
      }

      @Test
      void shouldHandleOr() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put("tricia", new User("trillian", "Tricia", "tricia@hog.org"));
        store.put("McMillan", new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
        store.put("dent", new User("arthur", "Arthur Dent", "arthur@hog.org"));

        List<User> all = store.query(
            Conditions.or(
              DISPLAY_NAME.eq("Tricia"),
              DISPLAY_NAME.eq("Trillian McMillan")
            )
          )
          .findAll();

        assertThat(all)
          .extracting("name")
          .containsExactly("trillian", "trillian");
      }


      @Test
      void shouldHandleOrWithMultipleStores() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Group").withIds("CoolGroup");
        User tricia = new User("trillian", "Tricia", "tricia@hog.org");
        User mcmillan = new User("trillian", "Trillian McMillan", "mcmillan@gmail.com");
        User dent = new User("arthur", "Arthur Dent", "arthur@hog.org");
        store.put("tricia", tricia);
        store.put("McMillan", mcmillan);
        store.put("dent", dent);

        SQLiteQueryableMutableStore<User> parallelStore = new StoreTestBuilder(connectionString, "sonia.Group").withIds("LameGroup");
        parallelStore.put("tricia", new User("trillian", "Trillian IAMINAPARALLELSTORE McMillan", "mcmillan@gmail.com"));

        List<User> result = store.query(
          Conditions.or(
            new LeafCondition<>(new QueryableStore.StringQueryField<>("mail"), Operator.EQ, "arthur@hog.org"),
            new LeafCondition<>(new QueryableStore.StringQueryField<>("mail"), Operator.EQ, "mcmillan@gmail.com"))
        ).findAll();

        assertThat(result).containsExactlyInAnyOrder(dent, mcmillan);
      }

      @Test
      void shouldHandleGroup() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Group")
          .withIds("42");
        store.put("tricia", new User("trillian", "Tricia", "tricia@hog.org"));
        new StoreTestBuilder(connectionString, "sonia.Group")
          .withIds("1337")
          .put("tricia", new User("trillian", "Trillian McMillan", "tricia@hog.org"));

        List<User> all = store.query().findAll();

        assertThat(all)
          .extracting("displayName")
          .containsExactly("Tricia");
      }

      @Test
      void shouldHandleGroupWithCondition() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Group")
          .withIds("42");
        store
          .put("tricia", new User("trillian", "Tricia", "tricia@hog.org"));
        new StoreTestBuilder(connectionString, "sonia.Group")
          .withIds("1337")
          .put("tricia", new User("trillian", "Trillian McMillan", "tricia@hog.org"));

        List<User> all = store.query(USER_NAME.eq("trillian")).findAll();

        assertThat(all)
          .extracting("displayName")
          .containsExactly("Tricia");
      }

      @Test
      void shouldHandleInArrayCondition() {
        SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
        store.put(new User("trillian", "McMillan", "tricia@hog.org"));
        store.put(new User("arthur", "Dent", "arthur@hog.org"));
        store.put(new User("zaphod", "Beeblebrox", "zaphod@hog.org"));

        List<User> all = store.query(
            USER_NAME.in("trillian", "arthur")
          )
          .findAll();

        assertThat(all)
          .extracting("name")
          .containsExactly("trillian", "arthur");
      }
    }

    @Test
    void shouldFindAllObjectsWithoutParentWithoutConditions() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      store.put("tricia", new User("trillian"));

      List<User> all = store.query().findAll();

      assertThat(all).hasSize(1);
    }

    @Test
    void shouldFindAllObjectsWithoutParentWithCondition() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      store.put("tricia", new User("trillian"));
      store.put("dent", new User("arthur"));

      List<User> all = store.query(USER_NAME.eq("trillian")).findAll();
      assertThat(all).hasSize(1);
    }

    @Test
    void shouldFindAllObjectsWithOneParentAndMultipleConditions() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, "sonia.Group").withIds("CoolGroup");
      User tricia = new User("trillian", "Tricia", "tricia@hog.org");
      User mcmillan = new User("trillian", "Trillian McMillan", "mcmillan@gmail.com");
      User dent = new User("arthur", "Arthur Dent", "arthur@hog.org");
      store.put("tricia", tricia);
      store.put("McMillan", mcmillan);
      store.put("dent", dent);

      List<User> result = store.query(
        Conditions.or(
          new LeafCondition<>(new QueryableStore.StringQueryField<>("mail"), Operator.EQ, "arthur@hog.org"),
          new LeafCondition<>(new QueryableStore.StringQueryField<>("mail"), Operator.EQ, "mcmillan@gmail.com"))
      ).findAll();

      assertThat(result).containsExactlyInAnyOrder(dent, mcmillan);
    }

    @Test
    void shouldFindAllObjectsWithoutParentWithMultipleConditions() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      store.put("tricia", new User("trillian", "Tricia", "tricia@hog.org"));
      store.put("McMillan", new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
      store.put("dent", new User("arthur", "Arthur Dent", "arthur@hog.org"));

      List<User> all = store.query(
          USER_NAME.eq("trillian"),
          DISPLAY_NAME.eq("Tricia")
        )
        .findAll();

      assertThat(all).hasSize(1);
    }

    @Test
    void shouldReturnIds() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString, Spaceship.class.getName())
        .withIds("hog");
      store.put("tricia", new User("trillian", "Tricia", "tricia@hog.org"));

      List<QueryableStore.Result<User>> results = store
        .query()
        .withIds()
        .findAll();

      assertThat(results).hasSize(1);
      QueryableStore.Result<User> result = results.get(0);
      assertThat(result.getParentId(Spaceship.class)).contains("hog");
      assertThat(result.getId()).isEqualTo("tricia");
      assertThat(result.getEntity().getName()).isEqualTo("trillian");
    }
  }

  @Nested
  class FindOne {
    @Test
    void shouldReturnEmptyOptionalIfNoResultFound() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
      assertThat(store.query(SPACESHIP_NAME.eq("Heart Of Gold")).findOne()).isEmpty();
    }

    @Test
    void shouldReturnOneResultIfOneIsGiven() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
      Spaceship expectedShip = new Spaceship("Heart Of Gold", Range.INNER_GALACTIC);
      store.put(expectedShip);
      Spaceship ship = store.query(SPACESHIP_NAME.eq("Heart Of Gold")).findOne().get();

      assertThat(ship).isEqualTo(expectedShip);
    }

    @Test
    void shouldThrowErrorIfMoreThanOneResultIsSaved() {
      SQLiteQueryableMutableStore<Spaceship> store = new StoreTestBuilder(connectionString).forClassWithIds(Spaceship.class);
      Spaceship expectedShip = new Spaceship("Heart Of Gold", Range.INNER_GALACTIC);
      Spaceship localShip = new Spaceship("Heart Of Gold", Range.SOLAR_SYSTEM);
      store.put(expectedShip);
      store.put(localShip);
      assertThatThrownBy(() -> store.query(SPACESHIP_NAME.eq("Heart Of Gold")).findOne().get())
        .isInstanceOf(QueryableStore.TooManyResultsException.class);
    }
  }

  @Nested
  class FindFirst {
    @Test
    void shouldFindFirst() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      User expectedUser = new User("trillian", "Tricia", "tricia@hog.org");

      store.put("1", expectedUser);
      store.put("2", new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
      store.put("3", new User("arthur", "Arthur Dent", "arthur@hog.org"));

      Optional<User> user = store.query(
          USER_NAME.eq("trillian")
        )
        .findFirst();

      assertThat(user).isEqualTo(Optional.of(expectedUser));
    }

    @Test
    void shouldFindFirstWithMatchingCondition() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      User expectedUser = new User("trillian", "Trillian McMillan", "mcmillan-alternate@gmail.com");

      store.put("1", new User("trillian", "Tricia", "tricia@hog.org"));
      store.put("2", new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
      store.put("3", expectedUser);
      store.put("4", new User("arthur", "Arthur Dent", "arthur@hog.org"));

      Optional<User> user = store.query(
          USER_NAME.eq("trillian"),
          MAIL.eq("mcmillan-alternate@gmail.com")
        )
        .findFirst();

      assertThat(user).isEqualTo(Optional.of(expectedUser));
    }


    @Test
    void shouldFindFirstWithMatchingLogicalCondition() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      User expectedUser = new User("trillian", "Trillian McMillan", "mcmillan@gmail.com");

      store.put("1", new User("trillian-old", "Tricia", "tricia@hog.org"));
      store.put("2", new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
      store.put("3", expectedUser);
      store.put("4", new User("arthur", "Arthur Dent", "arthur@hog.org"));
      store.put("5", new User("arthur", "Trillian McMillan", "mcmillan@gmail.com"));

      Optional<User> user = store.query(
        Conditions.and(
          Conditions.and(
            DISPLAY_NAME.eq("Trillian McMillan"),
            MAIL.eq("mcmillan@gmail.com")
          ),
          Conditions.not(
            ID.eq("1")
          )
        )
      ).findFirst();

      assertThat(user).isEqualTo(Optional.of(expectedUser));
    }

    @Test
    void shouldReturnEmptyOptionalIfNoResultFound() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      Optional<User> user = store.query(
          USER_NAME.eq("dave")
        )
        .findFirst();
      assertThat(user).isEmpty();
    }
  }

  @Nested
  class ForMaintenance {
    @Test
    void shouldUpdateRawJson() throws Exception {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      User user = new User("trillian", "Trillian McMillan", "mcmillan@gmail.com");
      store.put("1", user);

      try (MaintenanceIterator<User> iterator = store.iterateAll()) {
        assertThat(iterator.hasNext()).isTrue();
        MaintenanceStoreEntry<User> entry = iterator.next();
        assertThat(entry.getId()).isEqualTo("1");

        User userFromIterator = entry.get();
        userFromIterator.setName("dent");
        entry.update(userFromIterator);

        assertThat(iterator.hasNext()).isFalse();
      }
      User changedUser = store.get("1");
      assertThat(changedUser.getName()).isEqualTo("dent");
    }

    @Test
    void shouldUpdateRawJsonForItemWithParent() throws Exception {
      SQLiteQueryableMutableStore<User> subStore = new StoreTestBuilder(connectionString, Group.class.getName()).withIds("hitchhiker");
      User user = new User("trillian", "Trillian McMillan", "mcmillan@gmail.com");
      subStore.put("1", user);

      QueryableMaintenanceStore<User> maintenanceStore = new StoreTestBuilder(connectionString, Group.class.getName()).forMaintenanceWithSubIds();
      try (MaintenanceIterator<User> iterator = maintenanceStore.iterateAll()) {
        assertThat(iterator.hasNext()).isTrue();
        MaintenanceStoreEntry<User> entry = iterator.next();
        assertThat(entry.getId()).isEqualTo("1");

        User userFromIterator = entry.get();
        userFromIterator.setName("dent");
        entry.update(userFromIterator);

        assertThat(iterator.hasNext()).isFalse();
      }
      User changedUser = subStore.get("1");
      assertThat(changedUser.getName()).isEqualTo("dent");
    }

    @Test
    void shouldRemoveFromIteratorWithoutParent() {
      SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();
      store.put(new User("trillian", "Trillian McMillan", "mcmillan@gmail.com"));
      store.put(new User("dent", "Arthur Dent", "dent@gmail.com"));

      for (MaintenanceIterator<User> iter = store.iterateAll(); iter.hasNext(); ) {
        MaintenanceStoreEntry<User> next = iter.next();
        if (next.get().getName().equals("dent")) {
          iter.remove();
        }
      }

      assertThat(store.getAll())
        .values()
        .extracting("name")
        .containsExactly("trillian");
    }

    @Test
    void shouldRemoveFromIteratorWithParents() {
      StoreTestBuilder testStoreBuilder = new StoreTestBuilder(connectionString, Repository.class.getName(), Group.class.getName());
      SQLiteQueryableMutableStore<User> hogStore = testStoreBuilder.withIds("42", "hog");
      hogStore.put("trisha", new User("trillian", "Trillian McMillan", "mcmillan@hog.com"));
      hogStore.put("dent", new User("dent", "Arthur Dent", "dent@hog.com"));

      SQLiteQueryableMutableStore<User> earthStore = testStoreBuilder.withIds("42", "earth");
      earthStore.put("dent", new User("dent", "Arthur Dent", "dent@gmail.com"));

      QueryableMaintenanceStore<User> store = testStoreBuilder.forMaintenanceWithSubIds("42");

      for (MaintenanceIterator<User> iter = store.iterateAll(); iter.hasNext(); ) {
        MaintenanceStoreEntry<User> next = iter.next();
        if (next.get().getName().equals("dent") && next.getParentId(Group.class).get().equals("hog")) {
          iter.remove();
        }
      }

      assertThat(testStoreBuilder.withIds("42", "hog").getAll())
        .values()
        .extracting("name")
        .containsExactly("trillian");
      assertThat(testStoreBuilder.withIds("42", "earth").getAll())
        .values()
        .extracting("name")
        .containsExactly("dent");
    }

    @Test
    void shouldReadAll() {
      StoreTestBuilder testStoreBuilder = new StoreTestBuilder(connectionString, Repository.class.getName());
      SQLiteQueryableMutableStore<User> hogStore = testStoreBuilder.withIds("42");
      hogStore.put("trisha", new User("trillian", "Trillian McMillan", "mcmillan@hog.com"));
      hogStore.put("dent", new User("dent", "Arthur Dent", "dent@hog.com"));

      QueryableMaintenanceStore<User> store = testStoreBuilder.forMaintenanceWithSubIds("42");

      Collection<QueryableMaintenanceStore.Row<User>> rows = store.readAll();

      assertThat(rows)
        .extracting("id")
        .containsExactlyInAnyOrder("dent", "trisha");
      assertThat(rows)
        .extracting(QueryableMaintenanceStore.Row::getParentIds)
        .allSatisfy(strings -> assertThat(strings).containsExactly("42"));
      assertThat(rows)
        .extracting(QueryableMaintenanceStore.Row::getValue)
        .extracting("name")
        .containsExactlyInAnyOrder("trillian", "dent");
    }

    @Test
    void shouldReadAllWithMultipleIds() {
      StoreTestBuilder testStoreBuilder = new StoreTestBuilder(connectionString, Repository.class.getName(), Group.class.getName());
      SQLiteQueryableMutableStore<User> store1 = testStoreBuilder.withIds("42", "astronauts");
      store1.put("trisha", new User("trillian", "Trillian McMillan", "mcmillan@hog.com"));
      SQLiteQueryableMutableStore<User> store2 = testStoreBuilder.withIds("42", "earthlings");
      store2.put("dent", new User("dent", "Arthur Dent", "dent@hog.com"));

      QueryableMaintenanceStore<User> store = testStoreBuilder.forMaintenanceWithSubIds("42");

      Collection<QueryableMaintenanceStore.Row<User>> rows = store.readAll();

      Optional<QueryableMaintenanceStore.Row<User>> trisha = rows.stream()
        .filter(row -> row.getId().equals("trisha"))
        .findFirst();
      assertThat(trisha)
        .get()
        .extracting(QueryableMaintenanceStore.Row::getParentIds)
        .isEqualTo(new String[]{"42", "astronauts"});

      Optional<QueryableMaintenanceStore.Row<User>> dent = rows.stream()
        .filter(row -> row.getId().equals("dent"))
        .findFirst();
      assertThat(dent)
        .get()
        .extracting(QueryableMaintenanceStore.Row::getParentIds)
        .isEqualTo(new String[]{"42", "earthlings"});
    }

    @Test
    void shouldWriteAllForNewParent() {
      StoreTestBuilder testStoreBuilder = new StoreTestBuilder(connectionString, Repository.class.getName());

      QueryableMaintenanceStore<User> store = testStoreBuilder.forMaintenanceWithSubIds("42");
      store.writeAll(
        List.of(
          new QueryableMaintenanceStore.Row<>(new String[]{"23"}, "trisha", new User("trillian", "Trillian McMillan", "trisha@hog.com"))
        )
      );

      SQLiteQueryableMutableStore<User> hogStore = testStoreBuilder.withIds("42");
      Collection<QueryableMaintenanceStore.Row<User>> allValues = hogStore.readAll();
      assertThat(allValues)
        .extracting("value")
        .extracting("name")
        .containsExactly("trillian");
    }

    @Test
    void shouldWriteRawForNewParent() {
      StoreTestBuilder testStoreBuilder = new StoreTestBuilder(connectionString, Repository.class.getName());

      QueryableMaintenanceStore<User> store = testStoreBuilder.forMaintenanceWithSubIds("42");
      store.writeRaw(
        List.of(
          new QueryableMaintenanceStore.RawRow(new String[]{"23"}, "trisha", "{ \"name\": \"trillian\", \"displayName\": \"Trillian McMillan\", \"mail\": \"mcmillan@hog.com\" }")
        )
      );

      SQLiteQueryableMutableStore<User> hogStore = testStoreBuilder.withIds("42");
      Collection<QueryableMaintenanceStore.Row<User>> allValues = hogStore.readAll();
      assertThat(allValues)
        .extracting("value")
        .extracting("name")
        .containsExactly("trillian");
    }
  }

  private static final QueryableStore.IdQueryField<User> ID =
    new QueryableStore.IdQueryField<>();
  private static final QueryableStore.IdQueryField<User> GROUP =
    new QueryableStore.IdQueryField<>(Group.class);
  private static final QueryableStore.StringQueryField<User> USER_NAME =
    new QueryableStore.StringQueryField<>("name");
  private static final QueryableStore.StringQueryField<User> DISPLAY_NAME =
    new QueryableStore.StringQueryField<>("displayName");
  private static final QueryableStore.StringQueryField<User> MAIL =
    new QueryableStore.StringQueryField<>("mail");
  private static final QueryableStore.LongQueryField<User> CREATION_DATE =
    new QueryableStore.LongQueryField<>("creationDate");
  private static final QueryableStore.IntegerQueryField<User> CREATION_DATE_AS_INTEGER =
    new QueryableStore.IntegerQueryField<>("creationDate");
  private static final QueryableStore.BooleanQueryField<User> ACTIVE =
    new QueryableStore.BooleanQueryField<>("active");

  enum Range {
    SOLAR_SYSTEM, INNER_GALACTIC, INTER_GALACTIC
  }
}
