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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.repository.Repository;
import sonia.scm.store.QueryableMaintenanceStore;
import sonia.scm.user.User;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class SQLiteParallelizationTest {

  private String connectionString;

  @BeforeEach
  void init(@TempDir Path path) {
    connectionString = "jdbc:sqlite:" + path.toString() + "/test.db";
  }

  @Test
  void shouldTestParallelPutOperations() throws InterruptedException, ExecutionException, SQLException {
    int numThreads = 100;
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    List<Future<?>> futures = new ArrayList<>();

    SQLiteQueryableMutableStore<User> store = new StoreTestBuilder(connectionString).withIds();

    for (int i = 0; i < numThreads; i++) {
      final String userId = "user-" + i;
      final String userName = "User" + i;

      futures.add(executor.submit(() -> {
        try {
          store.transactional(() -> {
            store.put(userId, new User(userName));
            return true;
          });
        } catch (Exception e) {
          fail("Error storing user", e);
        }
      }));
    }

    for (Future<?> future : futures) {
      future.get();
    }
    executor.shutdown();

    int count = actualCount();
    assertEquals(numThreads, count, "All threads should have been successfully saved");
  }

  @Test
  void shouldWriteMultipleRowsConcurrently() throws InterruptedException, ExecutionException, SQLException {
    int numThreads = 100;
    int rowsPerThread = 50;
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    List<Future<?>> futures = new ArrayList<>();

    StoreTestBuilder testStoreBuilder = new StoreTestBuilder(connectionString, Repository.class.getName());
    QueryableMaintenanceStore<User> store = testStoreBuilder.forMaintenanceWithSubIds("42");

    for (int i = 0; i < numThreads; i++) {
      final int threadIndex = i;
      futures.add(executor.submit(() -> {
        List<QueryableMaintenanceStore.Row> rows = new ArrayList<>();
        try {
          for (int j = 1; j <= rowsPerThread; j++) {
            QueryableMaintenanceStore.Row<User> row = new QueryableMaintenanceStore.Row<>(
              new String[]{String.valueOf(threadIndex)},
              "user-" + threadIndex + "-" + j,
              new User("User" + threadIndex + "-" + j, "User " + threadIndex + "-" + j,
                "user" + threadIndex + "-" + j + "@example.com")
            );
            rows.add(row);
          }

          store.writeAll(rows);
        } catch (Exception e) {
          fail("Error writing rows", e);
        }
      }));
    }

    for (Future<?> future : futures) {
      future.get();
    }
    executor.shutdown();

    int expectedCount = numThreads * rowsPerThread;
    int count = actualCount();
    assertEquals(expectedCount, count, "Exactly " + expectedCount + " entries should have been saved");
  }

  private int actualCount() throws SQLException {
    int count;
    try (Connection conn = DriverManager.getConnection(connectionString);
         PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM sonia_scm_user_User_STORE");
         ResultSet rs = stmt.executeQuery()) {
      rs.next();
      count = rs.getInt(1);
    }
    return count;
  }
}

