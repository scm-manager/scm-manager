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
import java.util.Set;

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
  private SQLiteQueryableStoreFactory storeFactory;

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
    storeFactory = new SQLiteQueryableStoreFactory(
      connectionString,
      mapper,
      new UUIDKeyGenerator(),
      queryableTypeDescriptors
    );
  }

  @Override
  public void afterEach(ExtensionContext context) throws IOException {
    IOUtil.delete(tempDirectory.toFile());
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
}
