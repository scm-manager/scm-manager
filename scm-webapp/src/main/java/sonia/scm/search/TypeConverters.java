/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
