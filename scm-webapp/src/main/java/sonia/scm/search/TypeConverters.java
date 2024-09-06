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

package sonia.scm.search;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class TypeConverters {

  private TypeConverters() {
  }

  static TypeConverter create(Class<?> type) {
    List<FieldConverter> fieldConverters = new ArrayList<>();
    collectFields(fieldConverters, type);
    return new TypeConverter(fieldConverters);
  }

  private static void collectFields(List<FieldConverter> fieldConverters, Class<?> type) {
    Class<?> parent = type.getSuperclass();
    if (parent != null) {
      collectFields(fieldConverters, parent);
    }
    for (Field field : type.getDeclaredFields()) {
      Indexed indexed = field.getAnnotation(Indexed.class);
      if (indexed != null) {
        IndexableFieldFactory fieldFactory = IndexableFields.create(field, indexed);
        Method getter = findGetter(type, field);
        fieldConverters.add(new FieldConverter(field, getter, indexed, fieldFactory));
      }
    }
  }

  private static Method findGetter(Class<?> type, Field field) {
    String name = createGetterName(field);
    try {
      return type.getMethod(name);
    } catch (NoSuchMethodException ex) {
      throw new NonReadableFieldException("could not find getter for field", ex);
    }
  }

  private static String createGetterName(Field field) {
    String fieldName = field.getName();
    String prefix = "get";
    if (field.getType() == Boolean.TYPE) {
      prefix = "is";
    }
    return prefix + fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
  }

}
