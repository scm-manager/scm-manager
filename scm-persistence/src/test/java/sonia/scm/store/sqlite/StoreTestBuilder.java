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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mockito.Mockito;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.security.UUIDKeyGenerator;
import sonia.scm.store.IdGenerator;
import sonia.scm.store.QueryableMaintenanceStore;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStore;
import sonia.scm.user.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static sonia.scm.store.sqlite.QueryableTypeDescriptorTestData.createDescriptor;

class StoreTestBuilder {

  private final ObjectMapper mapper = getObjectMapper();

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, true);
    objectMapper.configure(WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper;
  }

  private final String connectionString;
  private final String[] parentClasses;
  private final IdGenerator idGenerator;

  private Collection<String> readOnlyRepositoryIds = new HashSet<>();

  StoreTestBuilder(String connectionString, String... parentClasses) {
    this(connectionString, IdGenerator.DEFAULT, parentClasses);
  }

  StoreTestBuilder(String connectionString, IdGenerator idGenerator, String... parentClasses) {
    this.connectionString = connectionString;
    this.idGenerator = idGenerator;
    this.parentClasses = parentClasses;
  }

  QueryableMutableStore<User> withIds(String... ids) {
    return forClassWithIds(User.class, ids);
  }

  QueryableStore<User> withSubIds(String... ids) {
    if (ids.length > parentClasses.length) {
      throw new IllegalArgumentException("id length should be at most " + parentClasses.length);
    }
    return createStoreFactory(User.class).getReadOnly(User.class, ids);
  }

  QueryableMaintenanceStore<User> forMaintenanceWithSubIds(String... ids) {
    if (ids.length > parentClasses.length) {
      throw new IllegalArgumentException("id length should be at most " + parentClasses.length);
    }
    return createStoreFactory(User.class).getForMaintenance(User.class, ids);
  }

  <T> QueryableMutableStore<T> forClassWithIds(Class<T> clazz, String... ids) {
    return createStoreFactory(clazz).getMutable(clazz, ids);
  }

  StoreTestBuilder readOnly(String... repositoryIds) {
    readOnlyRepositoryIds.addAll(List.of(repositoryIds));
    return this;
  }

  private <T> SQLiteQueryableStoreFactory createStoreFactory(Class<T> clazz) {
    RepositoryReadOnlyChecker readOnlyChecker;
    if (readOnlyRepositoryIds.isEmpty()) {
      readOnlyChecker = new RepositoryReadOnlyChecker();
    } else {
      readOnlyChecker = Mockito.mock(RepositoryReadOnlyChecker.class);
      when(readOnlyChecker.isReadOnly(argThat((String repoId) -> readOnlyRepositoryIds.contains(repoId))))
        .thenReturn(true);
      when(readOnlyChecker.isReadOnly(any(Repository.class))).thenCallRealMethod();
    }
    return new SQLiteQueryableStoreFactory(
      connectionString,
      mapper,
      new UUIDKeyGenerator(),
      List.of(createDescriptor("", clazz.getName(), parentClasses, idGenerator)),
      readOnlyChecker
    );
  }
}
