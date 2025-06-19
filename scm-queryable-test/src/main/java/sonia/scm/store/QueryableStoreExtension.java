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

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import sonia.scm.plugin.QueryableTypeDescriptor;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.security.UUIDKeyGenerator;
import sonia.scm.store.sqlite.SQLiteQueryableStoreFactory;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

/**
 * Loads {@link QueryableTypes} into a JUnit test suite.
 * <br/>
 * This extension also includes support for {@link Nested} classes: {@link QueryableTypes} attached to a nested class
 * are loaded before the types of its parent.
 */
public class QueryableStoreExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback {
  private final ObjectMapper mapper = getObjectMapper();
  private final Set<Class<?>> storeFactoryClasses = new HashSet<>();
  private Path tempDirectory;
  private Collection<QueryableTypeDescriptor> queryableTypeDescriptors;
  private QueryableStoreFactory storeFactory;
  private Collection<ClosedChecking> createdStores;

  private static ObjectMapper getObjectMapper() {
    // this should be the same as in ObjectMapperProvider
    return new ObjectMapper()
      .registerModule(new Jdk8Module())
      .registerModule(new JavaTimeModule())
      .setAnnotationIntrospector(createAnnotationIntrospector())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true)
      .setDateFormat(new StdDateFormat());
  }

  private static AnnotationIntrospector createAnnotationIntrospector() {
    return new AnnotationIntrospectorPair(
      new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance()),
      new JacksonAnnotationIntrospector()
    );
  }

  @Override
  public void beforeEach(ExtensionContext context) throws IOException {
    tempDirectory = Files.createTempDirectory("test");
    String connectionString = "jdbc:sqlite:" + tempDirectory.toString() + "/test.db";
    queryableTypeDescriptors = new ArrayList<>();
    addDescriptors(context);
    createdStores = new ArrayList<>();
    storeFactory = new ClosedCheckingQueryableStoreFactory(
      new SQLiteQueryableStoreFactory(
        connectionString,
        mapper,
        new UUIDKeyGenerator(),
        queryableTypeDescriptors,
        new RepositoryReadOnlyChecker()
      )
    );
  }

  @Override
  public void afterEach(ExtensionContext context) throws IOException {
    IOUtil.delete(tempDirectory.toFile());
    for (ClosedChecking store : createdStores) {
      store.assertClosed();
    }
  }

  private void addDescriptors(ExtensionContext context) {
    context.getTestClass().ifPresent(
      testClass -> {
        QueryableTypes annotation = testClass.getAnnotation(QueryableTypes.class);
        if (annotation != null) {
          queryableTypeDescriptors.addAll(stream(
            annotation
              .value()
          ).map(this::createDescriptor).toList());
        }
      }
    );

    context.getParent().ifPresent(this::addDescriptors);
  }

  private QueryableTypeDescriptor createDescriptor(Class<?> clazz) {
    QueryableType queryableAnnotation = clazz.getAnnotation(QueryableType.class);
    QueryableTypeDescriptor descriptor = new QueryableTypeDescriptor(
      queryableAnnotation.name(),
      clazz.getName(),
      stream(queryableAnnotation.value()).map(Class::getName).toArray(String[]::new),
      queryableAnnotation.idGenerator()
    );
    try {
      Class<?> storeFactoryClass = Class.forName(clazz.getName() + "StoreFactory");
      storeFactoryClasses.add(storeFactoryClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("class for store factory not found", e);
    }
    return descriptor;
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> requestedParameterType = parameterContext.getParameter().getType();
    return requestedParameterType.equals(QueryableStoreFactory.class)
      || storeFactoryClasses.contains(requestedParameterType);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> requestedParameterType = parameterContext.getParameter().getType();
    if (requestedParameterType.equals(QueryableStoreFactory.class)) {
      return storeFactory;
    } else if (storeFactoryClasses.contains(requestedParameterType)) {
      try {
        Constructor<?> constructor = requestedParameterType.getDeclaredConstructor(QueryableStoreFactory.class);
        constructor.setAccessible(true);
        return constructor.newInstance(storeFactory);
      } catch (Exception e) {
        throw new RuntimeException("failed to instantiate store factory", e);
      }
    } else {
      throw new ParameterResolutionException("unsupported parameter type");
    }
  }

  @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  public @interface QueryableTypes {
    Class<?>[] value();
  }

  private class ClosedCheckingQueryableStoreFactory implements QueryableStoreFactory {
    private final QueryableStoreFactory delegate;

    ClosedCheckingQueryableStoreFactory(QueryableStoreFactory delegate) {
      this.delegate = delegate;
    }

    @Override
    public <T> QueryableMaintenanceStore<T> getForMaintenance(Class<T> clazz, String... parentIds) {
      ClosedCheckingQueryableMaintenanceStore<T> store = new ClosedCheckingQueryableMaintenanceStore<>(delegate.getForMaintenance(clazz, parentIds));
      createdStores.add(store);
      return store;
    }

    @Override
    public <T> QueryableStore<T> getReadOnly(Class<T> clazz, String... parentIds) {
      ClosedCheckingQueryableStore<T> store = new ClosedCheckingQueryableStore<>(delegate.getReadOnly(clazz, parentIds));
      createdStores.add(store);
      return store;
    }

    @Override
    public <T> QueryableMutableStore<T> getMutable(Class<T> clazz, String... parentIds) {
      ClosedCheckingQueryableMutableStore<T> store = new ClosedCheckingQueryableMutableStore<>(delegate.getMutable(clazz, parentIds));
      createdStores.add(store);
      return store;
    }
  }

  private interface ClosedChecking {
    default void assertClosed() {
      if (!isClosed()) {
        throw new IllegalStateException("Store has not been closed. Use stores in a try-with-resources block or call close() manually.");
      }
    }

    boolean isClosed();
  }

  private static class ClosedCheckingQueryableMaintenanceStore<T> implements QueryableMaintenanceStore<T>, ClosedChecking {
    private final QueryableMaintenanceStore<T> delegate;

    private boolean closed = false;

    ClosedCheckingQueryableMaintenanceStore(QueryableMaintenanceStore<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void clear() {
      delegate.clear();
    }

    @Override
    public void close() {
      delegate.close();
      closed = true;
    }

    @Override
    public MaintenanceIterator<T> iterateAll() {
      return delegate.iterateAll();
    }

    @Override
    public Collection<Row<T>> readAll() throws SerializationException {
      return delegate.readAll();
    }

    @Override
    public <U> Collection<Row<U>> readAllAs(Class<U> type) throws SerializationException {
      return delegate.readAllAs(type);
    }

    @Override
    public Collection<RawRow> readRaw() {
      return delegate.readRaw();
    }

    @Override
    public void writeAll(Iterable<Row> rows) throws SerializationException {
      delegate.writeAll(rows);
    }

    @Override
    public void writeAll(Stream<Row> rows) throws SerializationException {
      delegate.writeAll(rows);
    }

    @Override
    public void writeRaw(Iterable<RawRow> rows) {
      delegate.writeRaw(rows);
    }

    @Override
    public void writeRaw(Stream<RawRow> rows) {
      delegate.writeRaw(rows);
    }

    @Override
    public boolean isClosed() {
      return closed;
    }
  }

  private static class ClosedCheckingQueryableStore<T> implements QueryableStore<T>, ClosedChecking {
    private final QueryableStore<T> delegate;

    private boolean closed = false;

    ClosedCheckingQueryableStore(QueryableStore<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void close() {
      delegate.close();
      closed = true;
    }

    @Override
    public Query<T, T, ?> query(Condition<T>... conditions) {
      return delegate.query(conditions);
    }

    @Override
    public boolean isClosed() {
      return closed;
    }
  }

  private static class ClosedCheckingQueryableMutableStore<T> implements QueryableMutableStore<T>, ClosedChecking {
    private final QueryableMutableStore<T> delegate;

    private boolean closed = false;

    ClosedCheckingQueryableMutableStore(QueryableMutableStore<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void close() {
      delegate.close();
      closed = true;
    }

    @Override
    public MutableQuery<T, ?> query(Condition<T>... conditions) {
      return delegate.query(conditions);
    }

    @Override
    public void transactional(BooleanSupplier callback) {
      delegate.transactional(callback);
    }

    @Override
    public Map<String, T> getAll() {
      return delegate.getAll();
    }

    @Override
    public void put(String id, T item) {
      delegate.put(id, item);
    }

    @Override
    public String put(T item) {
      return delegate.put(item);
    }

    @Override
    public void clear() {
      delegate.clear();
    }

    @Override
    public T get(String id) {
      return delegate.get(id);
    }

    @Override
    public Optional<T> getOptional(String id) {
      return delegate.getOptional(id);
    }

    @Override
    public void remove(String id) {
      delegate.remove(id);
    }

    @Override
    public boolean isClosed() {
      return closed;
    }
  }
}
