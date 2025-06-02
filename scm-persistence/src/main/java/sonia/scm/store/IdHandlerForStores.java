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

import sonia.scm.security.KeyGenerator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.String.format;

public class IdHandlerForStores<T> {

  private static final Map<Class<?>, Optional<Field>> FIELD_CACHE = new HashMap<>();

  private final KeyGenerator keyGenerator;

  private final Function<T, String> idExtractor;
  private final BiConsumer<T, String> idSetter;
  private final BiConsumer<String, T> doPut;

  @SuppressWarnings("unchecked")
  public IdHandlerForStores(Class<T> clazz, KeyGenerator keyGenerator, BiConsumer<String, T> doPut) {
    this.idExtractor = getIdExtractor(clazz);
    this.idSetter = getIdSetter(clazz);
    this.keyGenerator = keyGenerator;
    this.doPut = doPut;
  }

  private static <T> Function<T, String> getIdExtractor(Class<?> clazz) {
    return FIELD_CACHE.computeIfAbsent(clazz, IdHandlerForStores::getIdField)
      .map(field -> (Function<T, String>) item -> {
        try {
          return (String) field.get(item);
        } catch (IllegalAccessException e) {
          throw new StoreException("Failed to get id from object", e);
        }
      })
      .orElse(object -> null);
  }

  private static <T> BiConsumer<T, String> getIdSetter(Class<?> clazz) {
    return FIELD_CACHE.computeIfAbsent(clazz, IdHandlerForStores::getIdField)
      .map(field -> (BiConsumer<T, String>) (object, id) -> {
        try {
          field.set(object, id);
        } catch (IllegalAccessException e) {
          throw new StoreException("Failed to set id on object", e);
        }
      })
      .orElse((object, id) -> {
        // do nothing
      });
  }

  private static Optional<Field> getIdField(Class<?> clazz) {
    for (var field : clazz.getDeclaredFields()) {
      if (field.isAnnotationPresent(Id.class)) {
        if (field.getType() != String.class) {
          throw new StoreException(format("The field '%s' annotated with @Id in class %s must be of type String.", field.getName(), clazz.getName()));
        }
        field.setAccessible(true);
        return Optional.of(field);
      }
    }
    if (clazz.getSuperclass() != null) {
      return getIdField(clazz.getSuperclass());
    }
    return Optional.empty();
  }

  public String put(T item) {
    String idFromItem = idExtractor.apply(item);
    if (idFromItem == null) {
      String id = keyGenerator.createKey();
      put(id, item);
      return id;
    } else {
      put(idFromItem, item);
      return idFromItem;
    }
  }

  public void put(String id, T item) {
    idSetter.accept(item, id);
    doPut.accept(id, item);
  }
}
