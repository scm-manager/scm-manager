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

package sonia.scm.store;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.user.User;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreInvocationMicrometerWrapperTest {

  @Mock
  private MeterRegistry meterRegistry;
  @Mock(answer = RETURNS_SELF)
  private Timer.Builder timerBuilder;
  @Mock
  private Timer timer;

  @BeforeEach
  void setUpTimer() {
    when(timerBuilder.register(any()))
      .thenReturn(timer);
    when(timer.record(any(Supplier.class)))
      .thenAnswer(i -> i.getArgument(0, Supplier.class).get());
  }

  @Nested
  class ForSimpleStore {

    @Mock
    private DataStore<User> dataStore;
    @Mock
    private TypedStoreParameters<User> storeParameters;

    @BeforeEach
    void setUpStore() {
      when(storeParameters.getName())
        .thenReturn("users");
      when(dataStore.get("42")).thenReturn(new User("dent"));
    }

    @Test
    void shouldMeasureDirectCallForGlobalStore() {
      StoreInvocationMicrometerWrapper handler =
        new StoreInvocationMicrometerWrapper(
          "data",
          storeParameters,
          dataStore,
          meterRegistry,
          () -> timerBuilder
        );
      DataStore<User> store = (DataStore<User>) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{DataStore.class}, handler);
      User result = store
        .get("42");

      assertThat(result)
        .extracting(User::getName)
        .isEqualTo("dent");

      verify(timerBuilder).description("Time taken for store operation in data");
      verify(timerBuilder).tag("method", "get");
      verify(timerBuilder).tag("type", "data");
      verify(timerBuilder).tag("storeName", "users");
      verify(timerBuilder).tag("repositoryId", "none");
      verify(timerBuilder).tag("namespace", "none");

      verify(timer, only()).record(any(Supplier.class));
    }

    @Test
    void shouldMeasureDirectCallForRepositoryStore() {
      when(storeParameters.getRepositoryId())
        .thenReturn("hog");
      StoreInvocationMicrometerWrapper handler =
        new StoreInvocationMicrometerWrapper(
          "data",
          storeParameters,
          dataStore,
          meterRegistry,
          () -> timerBuilder
        );
      DataStore<User> store = (DataStore<User>) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{DataStore.class}, handler);
      User result = store
        .get("42");

      assertThat(result)
        .extracting(User::getName)
        .isEqualTo("dent");

      verify(timerBuilder).description("Time taken for store operation in data");
      verify(timerBuilder).tag("method", "get");
      verify(timerBuilder).tag("repositoryId", "hog");
      verify(timerBuilder).tag("namespace", "none");

      verify(timer, only()).record(any(Supplier.class));
    }

    @Test
    void shouldMeasureDirectCallForNamespaceStore() {
      when(storeParameters.getNamespace())
        .thenReturn("intergalactic");
      StoreInvocationMicrometerWrapper handler =
        new StoreInvocationMicrometerWrapper(
          "data",
          storeParameters,
          dataStore,
          meterRegistry,
          () -> timerBuilder
        );
      DataStore<User> store = (DataStore<User>) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{DataStore.class}, handler);
      User result = store
        .get("42");

      assertThat(result)
        .extracting(User::getName)
        .isEqualTo("dent");

      verify(timerBuilder).description("Time taken for store operation in data");
      verify(timerBuilder).tag("method", "get");
      verify(timerBuilder).tag("namespace", "intergalactic");
      verify(timerBuilder).tag("repositoryId", "none");

      verify(timer, only()).record(any(Supplier.class));
    }
  }

  @Nested
  class ForQueryableStore {

    @Mock
    private QueryableStore<User> queryableStore;
    @Mock
    private QueryableStore.Query<User, User, ?> query;

    @BeforeEach
    void setUpStore() {
      when(queryableStore.query())
        .thenAnswer(i -> query);
      when(query.findAll())
        .thenReturn(List.of(new User("dent")));
    }

    @Test
    void shouldMeasureDirectCall() {
      StoreInvocationMicrometerWrapper handler =
        new StoreInvocationMicrometerWrapper(
          "queryable",
          User.class,
          new String[]{"intergalactic", "hog"},
          queryableStore,
          meterRegistry,
          () -> timerBuilder
        );
      QueryableStore<User> store = (QueryableStore<User>) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{QueryableStore.class}, handler);
      List<User> result = store
        .query()
        .findAll();

      assertThat(result)
        .extracting(User::getName)
        .containsExactly("dent");

      verify(timerBuilder).description("Time taken for store operation in queryable");
      verify(timerBuilder).tag("method", "findAll");
      verify(timerBuilder).tag("storeClass", "sonia.scm.user.User");
      verify(timerBuilder).tag("parentIds", "intergalactic,hog");

      verify(timer, only()).record(any(Supplier.class));
    }

    @Test
    void shouldMeasureCallAfterOrder() {
      when(query.orderBy(any(), any(QueryableStore.Order.class)))
        .thenAnswer(i -> query);

      StoreInvocationMicrometerWrapper handler =
        new StoreInvocationMicrometerWrapper(
          "queryable",
          User.class,
          new String[]{"intergalactic", "hog"},
          queryableStore,
          meterRegistry,
          () -> timerBuilder
        );
      QueryableStore store = (QueryableStore) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{QueryableStore.class}, handler);
      List<User> result = store
        .query()
        .orderBy(new QueryableStore.IdQueryField(), QueryableStore.Order.DESC)
        .findAll();

      assertThat(result)
        .extracting(User::getName)
        .containsExactly("dent");

      verify(timerBuilder).description("Time taken for store operation in queryable");
      verify(timerBuilder).tag("method", "findAll");

      verify(timer, only()).record(any(Supplier.class));
    }

    @Test
    void shouldMeasureCallAfterMultipleIndirection() {
      when(query.orderBy(any(), any(QueryableStore.Order.class)))
        .thenAnswer(i -> query);
      when(query.distinct())
        .thenAnswer(i -> query);
      when(query.project(any()))
        .thenAnswer(i -> query);

      StoreInvocationMicrometerWrapper handler =
        new StoreInvocationMicrometerWrapper(
          "queryable",
          User.class,
          new String[]{"intergalactic", "hog"},
          queryableStore,
          meterRegistry,
          () -> timerBuilder
        );
      QueryableStore store = (QueryableStore) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{QueryableStore.class}, handler);
      List<User> result = store
        .query()
        .orderBy(new QueryableStore.IdQueryField(), QueryableStore.Order.DESC)
        .distinct()
        .project(new QueryableStore.IdQueryField())
        .findAll();

      assertThat(result)
        .extracting(User::getName)
        .containsExactly("dent");

      verify(timerBuilder).description("Time taken for store operation in queryable");
      verify(timerBuilder).tag("method", "findAll");

      verify(timer, only()).record(any(Supplier.class));
    }
  }

  @Nested
  class ForQueryableMutableStore {

    @Mock
    private QueryableMutableStore<User> queryableMutableStore;
    @Mock
    private QueryableMutableStore.MutableQuery<User, ?> query;

    @BeforeEach
    void setUpStore() {
      when(queryableMutableStore.query())
        .thenAnswer(i -> query);
    }

    @Test
    void shouldMeasureFinalCall() {
      when(query.orderBy(any(), any(QueryableStore.Order.class)))
        .thenAnswer(i -> query);

      StoreInvocationMicrometerWrapper handler =
        new StoreInvocationMicrometerWrapper(
          "queryable",
          User.class,
          new String[]{"intergalactic", "hog"},
          queryableMutableStore,
          meterRegistry,
          () -> timerBuilder
        );
      QueryableMutableStore<User> store = (QueryableMutableStore) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{QueryableMutableStore.class}, handler);
      store
        .query()
        .orderBy(new QueryableStore.IdQueryField(), QueryableStore.Order.DESC)
        .retain(100);

      verify(timerBuilder).description("Time taken for store operation in queryable");
      verify(timerBuilder).tag("method", "retain");

      verify(timer, only()).record(any(Supplier.class));
    }
  }
}
