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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.synchronizedMap;

class IdFieldFinder {

  private static final Map<Class<?>, Optional<Field>> FIELD_CACHE = synchronizedMap(new HashMap<>());

  Optional<Field> getIdField(Class<?> clazz) {
    return FIELD_CACHE.computeIfAbsent(clazz, this::findIdField);
  }

  @SuppressWarnings("java:S3011") // we need reflection to access the field
  private Optional<Field> findIdField(Class<?> clazz) {
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
      return findIdField(clazz.getSuperclass());
    }
    return Optional.empty();
  }
}
