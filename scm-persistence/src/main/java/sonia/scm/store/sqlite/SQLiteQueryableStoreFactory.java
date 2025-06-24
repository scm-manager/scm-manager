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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.JDBC;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ConfigValue;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.QueryableTypeDescriptor;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.QueryableMaintenanceStore;
import sonia.scm.store.QueryableStoreFactory;
import sonia.scm.store.StoreException;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;

@Slf4j
@Singleton
public class SQLiteQueryableStoreFactory implements QueryableStoreFactory {

  public static final String DEFAULT_MAX_POOL_SIZE = "10";
  public static final int MIN_IDLE = 0;
  public static final String DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS = "30";
  public static final String DEFAULT_IDLE_TIMEOUT_IN_SECONDS = "600";
  public static final String DEFAULT_MAX_LIFETIME_IN_SECONDS = "1800";
  public static final String DEFAULT_LEAK_DETECTION_THRESHOLD_IN_SECONDS = "30";
  private final ObjectMapper objectMapper;
  private final KeyGenerator keyGenerator;
  private final DataSource dataSource;
  private final RepositoryReadOnlyChecker readOnlyChecker;

  private final Map<String, QueryableTypeDescriptor> queryableTypes = new HashMap<>();

  private final ReadWriteLock lock = new LoggingReadWriteLock(new ReentrantReadWriteLock());

  @Inject
  public SQLiteQueryableStoreFactory(SCMContextProvider contextProvider,
                                     PluginLoader pluginLoader,
                                     ObjectMapper objectMapper,
                                     KeyGenerator keyGenerator,
                                     RepositoryReadOnlyChecker readOnlyChecker,
                                     @ConfigValue(key = "queryableStore.maxPoolSize", defaultValue = DEFAULT_MAX_POOL_SIZE) int maxPoolSize,
                                     @ConfigValue(key = "queryableStore.connectionTimeout", defaultValue = DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS) int connectionTimeoutInSeconds,
                                     @ConfigValue(key = "queryableStore.idleTimeout", defaultValue = DEFAULT_IDLE_TIMEOUT_IN_SECONDS) int idleTimeoutInSeconds,
                                     @ConfigValue(key = "queryableStore.maxLifetime", defaultValue = DEFAULT_MAX_LIFETIME_IN_SECONDS) int maxLifetimeInSeconds,
                                     @ConfigValue(key = "queryableStore.leakDetectionThreshold", defaultValue = DEFAULT_LEAK_DETECTION_THRESHOLD_IN_SECONDS) int leakDetectionThresholdInSeconds
  ) {
    this(
      "jdbc:sqlite:" + contextProvider.resolve(Path.of("scm.db")) + "?shared_cache=true&journal_mode=WAL",
      objectMapper,
      keyGenerator,
      pluginLoader.getExtensionProcessor().getQueryableTypes(),
      readOnlyChecker,
      maxPoolSize,
      connectionTimeoutInSeconds,
      idleTimeoutInSeconds,
      maxLifetimeInSeconds,
      leakDetectionThresholdInSeconds
    );
  }

  @VisibleForTesting
  public SQLiteQueryableStoreFactory(String connectionString,
                                     ObjectMapper objectMapper,
                                     KeyGenerator keyGenerator,
                                     Iterable<QueryableTypeDescriptor> queryableTypeIterable,
                                     RepositoryReadOnlyChecker readOnlyChecker) {
    this(connectionString, objectMapper, keyGenerator, queryableTypeIterable, readOnlyChecker, 10, 30, 600, 1800, 30);
  }

  private SQLiteQueryableStoreFactory(String connectionString,
                                      ObjectMapper objectMapper,
                                      KeyGenerator keyGenerator,
                                      Iterable<QueryableTypeDescriptor> queryableTypeIterable,
                                      RepositoryReadOnlyChecker readOnlyChecker,
                                      int maxPoolSize,
                                      int connectionTimeoutInSeconds,
                                      int idleTimeoutInSeconds,
                                      int maxLifetimeInSeconds,
                                      int leakDetectionThresholdInSeconds) {
    HikariConfig config = createConnectionPoolConfig(
      connectionString,
      maxPoolSize,
      connectionTimeoutInSeconds,
      idleTimeoutInSeconds,
      maxLifetimeInSeconds,
      leakDetectionThresholdInSeconds);
    this.readOnlyChecker = readOnlyChecker;
    this.dataSource = new HikariDataSource(config);

    this.objectMapper = objectMapper
      .copy()
      .configure(WRITE_DATES_AS_TIMESTAMPS, true)
      .configure(WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
      .configure(READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    this.keyGenerator = keyGenerator;
    Connection connection = openDefaultConnection();
    try {
      TableCreator tableCreator = new TableCreator(connection);
      for (QueryableTypeDescriptor queryableTypeDescriptor : queryableTypeIterable) {
        queryableTypes.put(queryableTypeDescriptor.getClazz(), queryableTypeDescriptor);
        tableCreator.initializeTable(queryableTypeDescriptor);
      }
    } finally {
      try {
        connection.close();
      } catch (SQLException e) {
        log.warn("could not close connection", e);
      }
    }
  }

  private static HikariConfig createConnectionPoolConfig(String connectionString,
                                                         int maxPoolSize,
                                                         int connectionTimeoutInSeconds,
                                                         int idleTimeoutInSeconds,
                                                         int maxLifetimeInSeconds,
                                                         int leakDetectionThresholdInSeconds) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(connectionString);
    config.setMaximumPoolSize(maxPoolSize);
    config.setMinimumIdle(MIN_IDLE);
    config.setConnectionTimeout(connectionTimeoutInSeconds * 1000L);
    config.setIdleTimeout(idleTimeoutInSeconds * 1000L);
    config.setMaxLifetime(maxLifetimeInSeconds * 1000L);
    config.setConnectionTestQuery("SELECT 1");
    config.setPoolName("SCMM_SQLitePool");
    config.setDriverClassName(JDBC.class.getName());
    // If a connection is held for longer than 30 seconds, HikariCP will log a warning:
    config.setLeakDetectionThreshold(leakDetectionThresholdInSeconds * 1000L);
    return config;
  }

  private Connection openDefaultConnection() {
    try {
      log.debug("open connection");
      Connection connection = dataSource.getConnection();
      connection.setAutoCommit(true);
      return connection;
    } catch (SQLException e) {
      throw new StoreException("could not connect to database", e);
    }
  }

  @Override
  public <T> SQLiteQueryableStore<T> getReadOnly(Class<T> clazz, String... parentIds) {
    QueryableTypeDescriptor queryableTypeDescriptor = getQueryableTypeDescriptor(clazz);
    return new SQLiteQueryableStore<>(
      objectMapper,
      openDefaultConnection(),
      clazz,
      queryableTypeDescriptor,
      parentIds,
      lock,
      mustBeReadOnly(queryableTypeDescriptor, parentIds)
    );
  }

  @Override
  public <T> QueryableMaintenanceStore<T> getForMaintenance(Class<T> clazz, String... parentIds) {
    QueryableTypeDescriptor queryableTypeDescriptor = getQueryableTypeDescriptor(clazz);
    return new SQLiteQueryableStore<>(
      objectMapper,
      openDefaultConnection(),
      clazz,
      queryableTypeDescriptor,
      parentIds,
      lock,
      mustBeReadOnly(queryableTypeDescriptor, parentIds)
    );
  }

  @Override
  public <T> SQLiteQueryableMutableStore<T> getMutable(Class<T> clazz, String... parentIds) {
    QueryableTypeDescriptor queryableTypeDescriptor = getQueryableTypeDescriptor(clazz);
    return new SQLiteQueryableMutableStore<>(
      objectMapper,
      keyGenerator,
      openDefaultConnection(),
      clazz,
      queryableTypeDescriptor,
      parentIds,
      lock,
      mustBeReadOnly(queryableTypeDescriptor, parentIds)
    );
  }

  private boolean mustBeReadOnly(QueryableTypeDescriptor queryableTypeDescriptor, String... parentIds) {
    for (int i = 0; i < queryableTypeDescriptor.getTypes().length; i++) {
      if (queryableTypeDescriptor.getTypes()[i].startsWith(Repository.class.getName()) && parentIds.length > i) {
        String repositoryId = parentIds[i];
        if (repositoryId != null && readOnlyChecker.isReadOnly(repositoryId)) {
          return true;
        }
      }
    }
    return false;
  }

  private <T> QueryableTypeDescriptor getQueryableTypeDescriptor(Class<T> clazz) {
    return queryableTypes.get(clazz.getName().replace('$', '.'));
  }
}
