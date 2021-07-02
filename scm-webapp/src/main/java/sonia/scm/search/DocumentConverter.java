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

import com.google.common.base.Strings;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptySet;

@Singleton
public class DocumentConverter {

  private final Map<Class<?>, TypeConverter> typeConverter = new ConcurrentHashMap<>();

  Document convert(Object object) {
    TypeConverter converter = typeConverter.computeIfAbsent(object.getClass(), this::createTypeConverter);
    try {
      return converter.convert(object);
    } catch (IllegalAccessException | InvocationTargetException ex) {
      throw new SearchEngineException("failed to create document", ex);
    }
  }

  private TypeConverter createTypeConverter(Class<?> type) {
    List<FieldConverter> fieldConverters = new ArrayList<>();
    collectFields(fieldConverters, type);
    return new TypeConverter(fieldConverters);
  }

  private void collectFields(List<FieldConverter> fieldConverters, Class<?> type) {
    Class<?> parent = type.getSuperclass();
    if (parent != null) {
      collectFields(fieldConverters, parent);
    }
    for (Field field : type.getDeclaredFields()) {
      Indexed indexed = field.getAnnotation(Indexed.class);
      if (indexed != null) {
        IndexableFieldFactory fieldFactory = IndexableFieldFactories.create(field, indexed);
        Method getter = findGetter(type, field);
        fieldConverters.add(new FieldConverter(field, getter, indexed, fieldFactory));
      }
    }
  }

  private Method findGetter(Class<?> type, Field field) {
    String name = createGetterName(field);
    try {
      return type.getMethod(name);
    } catch (NoSuchMethodException ex) {
      throw new NonReadableFieldException("could not find getter for field", ex);
    }
  }

  private String createGetterName(Field field) {
    String fieldName = field.getName();
    String prefix = "get";
    if (field.getType() == Boolean.TYPE) {
      prefix = "is";
    }
    return prefix + fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
  }

  private static class TypeConverter {

    private final List<FieldConverter> fieldConverters;

    private TypeConverter(List<FieldConverter> fieldConverters) {
      this.fieldConverters = fieldConverters;
    }

    public Document convert(Object object) throws IllegalAccessException, InvocationTargetException {
      Document document = new Document();
      for (FieldConverter fieldConverter : fieldConverters) {
        for (IndexableField field : fieldConverter.convert(object)) {
          document.add(field);
        }
      }
      return document;
    }
  }

  private static class FieldConverter {

    private final Method getter;
    private final IndexableFieldFactory fieldFactory;
    private final String name;

    private FieldConverter(Field field, Method getter, Indexed indexed, IndexableFieldFactory fieldFactory) {
      this.getter = getter;
      this.fieldFactory = fieldFactory;
      this.name = createName(field, indexed);
    }

    private String createName(Field field, Indexed indexed) {
      String nameFromAnnotation = indexed.name();
      if (Strings.isNullOrEmpty(nameFromAnnotation)) {
        return field.getName();
      }
      return nameFromAnnotation;
    }

    Iterable<IndexableField> convert(Object object) throws IllegalAccessException, InvocationTargetException {
      Object value = getter.invoke(object);
      if (value != null) {
        return fieldFactory.create(name, value);
      }
      return emptySet();
    }

  }
}
