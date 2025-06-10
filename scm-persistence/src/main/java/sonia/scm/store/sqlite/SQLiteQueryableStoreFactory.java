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
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.QueryableTypeDescriptor;
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

  private final ObjectMapper objectMapper;
  private final KeyGenerator keyGenerator;
  private final DataSource dataSource;

  private final Map<String, QueryableTypeDescriptor> queryableTypes = new HashMap<>();

  private final ReadWriteLock lock = new LoggingReadWriteLock(new ReentrantReadWriteLock());

  @Inject
  public SQLiteQueryableStoreFactory(SCMContextProvider contextProvider,
                                     PluginLoader pluginLoader,
                                     ObjectMapper objectMapper,
                                     KeyGenerator keyGenerator) {
    this(
      "jdbc:sqlite:" + contextProvider.resolve(Path.of("scm.db")),
      objectMapper,
      keyGenerator,
      pluginLoader.getExtensionProcessor().getQueryableTypes()
    );
  }

  @VisibleForTesting
  public SQLiteQueryableStoreFactory(String connectionString,
                                     ObjectMapper objectMapper,
                                     KeyGenerator keyGenerator,
                                     Iterable<QueryableTypeDescriptor> queryableTypeIterable) {
    SQLiteConfig config = new SQLiteConfig();
    config.setSharedCache(true);
    config.setJournalMode(SQLiteConfig.JournalMode.WAL);

    this.dataSource = new SQLiteDataSource(
      config
    );
    ((SQLiteDataSource) dataSource).setUrl(connectionString);
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
    return new SQLiteQueryableStore<>(objectMapper, openDefaultConnection(), clazz, getQueryableTypeDescriptor(clazz), parentIds, lock);
  }

  @Override
  public <T> QueryableMaintenanceStore<T> getForMaintenance(Class<T> clazz, String... parentIds) {
    return new SQLiteQueryableStore<>(objectMapper, openDefaultConnection(), clazz, getQueryableTypeDescriptor(clazz), parentIds, lock);
  }

  @Override
  public <T> SQLiteQueryableMutableStore<T> getMutable(Class<T> clazz, String... parentIds) {
    return new SQLiteQueryableMutableStore<>(objectMapper, keyGenerator, openDefaultConnection(), clazz, getQueryableTypeDescriptor(clazz), parentIds, lock);
  }

  private <T> QueryableTypeDescriptor getQueryableTypeDescriptor(Class<T> clazz) {
    return queryableTypes.get(clazz.getName().replace('$', '.'));
  }
}
