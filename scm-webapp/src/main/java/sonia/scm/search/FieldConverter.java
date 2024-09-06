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

import com.google.common.base.Strings;
import org.apache.lucene.index.IndexableField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Collections.emptySet;

final class FieldConverter {

  private final Method getter;
  private final IndexableFieldFactory fieldFactory;
  private final String name;

  FieldConverter(Field field, Method getter, Indexed indexed, IndexableFieldFactory fieldFactory) {
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
