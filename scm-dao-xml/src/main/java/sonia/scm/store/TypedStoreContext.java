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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.xml.XmlStreams;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
final class TypedStoreContext<T> {

  private final JAXBContext jaxbContext;
  private final TypedStoreParameters<T> parameters;

  private static final Map<Class<?>, JAXBContext> contextCache = new HashMap<>();

  private TypedStoreContext(JAXBContext jaxbContext, TypedStoreParameters<T> parameters) {
    this.jaxbContext = jaxbContext;
    this.parameters = parameters;
  }

  static <T> TypedStoreContext<T> of(TypedStoreParameters<T> parameters) {
    JAXBContext jaxbContext;
    synchronized (contextCache) {
      jaxbContext = contextCache.computeIfAbsent(parameters.getType(), type -> createJaxbContext(parameters));
    }
    return new TypedStoreContext<>(jaxbContext, parameters);
  }

  private static <T> JAXBContext createJaxbContext(TypedStoreParameters<T> parameters) {
    try {
      return JAXBContext.newInstance(parameters.getType());
    } catch (JAXBException e) {
      throw new StoreException("failed to create context for store", e);
    }
  }

  T unmarshal(File file) {
    log.trace("unmarshal file {}", file);
    AtomicReference<T> ref = new AtomicReference<>();
    withUnmarshaller(unmarshaller -> {
      T value = parameters.getType().cast(unmarshaller.unmarshal(file));
      ref.set(value);
    });
    return ref.get();
  }

  void marshal(Object object, File file) {
    log.trace("marshal file {}", file);
    withMarshaller(marshaller -> marshaller.marshal(object, XmlStreams.createWriter(file)));
  }

  void withMarshaller(ThrowingConsumer<Marshaller> consumer) {
    Marshaller marshaller = createMarshaller();
    withClassLoader(consumer, marshaller);
  }

  void withUnmarshaller(ThrowingConsumer<Unmarshaller> consumer) {
    Unmarshaller unmarshaller = createUnmarshaller();
    withClassLoader(consumer, unmarshaller);
  }

  Class<?> getType() {
    return parameters.getType();
  }

  private <C> void withClassLoader(ThrowingConsumer<C> consumer, C consume) {
    ClassLoader contextClassLoader = null;
    Optional<ClassLoader> classLoader = parameters.getClassLoader();
    if (classLoader.isPresent()) {
      contextClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader.get());
    }
    try {
      consumer.consume(consume);
    } catch (Exception e) {
      throw new StoreException("failure during marshalling/unmarshalling", e);
    } finally {
      if (contextClassLoader != null) {
        Thread.currentThread().setContextClassLoader(contextClassLoader);
      }
    }
  }

  Marshaller createMarshaller() {
    try {
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      for (XmlAdapter<?, ?> adapter : parameters.getAdapters()) {
        marshaller.setAdapter(adapter);
      }
      return marshaller;
    } catch (JAXBException e) {
      throw new StoreException("could not create marshaller", e);
    }
  }

  private Unmarshaller createUnmarshaller() {
    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      for (XmlAdapter<?, ?> adapter : parameters.getAdapters()) {
        unmarshaller.setAdapter(adapter);
      }
      return unmarshaller;
    } catch (JAXBException e) {
      throw new StoreException("could not create unmarshaller", e);
    }
  }

  @FunctionalInterface
  interface ThrowingConsumer<T> {
    @SuppressWarnings("java:S112") // we need to throw Exception here
    void consume(T item) throws Exception;
  }

}
