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

import jakarta.annotation.Nonnull;
import org.apache.lucene.index.IndexableField;

import java.time.Instant;
import java.util.Locale;

final class ValueExtractors {

  private ValueExtractors() {
  }

  static ValueExtractor create(String name, Class<?> type) {
    if (TypeCheck.isString(type)) {
      return stringExtractor(name);
    } else if (TypeCheck.isLong(type)) {
      return longExtractor(name);
    } else if (TypeCheck.isInteger(type)) {
      return integerExtractor(name);
    } else if (TypeCheck.isBoolean(type)) {
      return booleanExtractor(name);
    } else if (TypeCheck.isInstant(type)) {
      return instantExtractor(name);
    } else if (type.isEnum()) {
      return enumExtractor(name, type);
    } else {
      throw new UnsupportedTypeOfFieldException(type, name);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static ValueExtractor enumExtractor(String name, Class type) {
    return doc -> {
      String value = doc.get(name);
      if (value != null) {
        return Enum.valueOf(type, value.toUpperCase(Locale.ENGLISH));
      }
      return null;
    };
  }

  @Nonnull
  private static ValueExtractor stringExtractor(String name) {
    return doc -> doc.get(name);
  }

  @Nonnull
  private static ValueExtractor instantExtractor(String name) {
    return doc -> {
      IndexableField field = doc.getField(name);
      if (field != null) {
        return Instant.ofEpochMilli(field.numericValue().longValue());
      }
      return null;
    };
  }

  @Nonnull
  private static ValueExtractor booleanExtractor(String name) {
    return doc -> Boolean.parseBoolean(doc.get(name));
  }

  @Nonnull
  private static ValueExtractor integerExtractor(String name) {
    return doc -> {
      IndexableField field = doc.getField(name);
      if (field != null) {
        return field.numericValue().intValue();
      }
      return null;
    };
  }

  @Nonnull
  private static ValueExtractor longExtractor(String name) {
    return doc -> {
      IndexableField field = doc.getField(name);
      if (field != null) {
        return field.numericValue().longValue();
      }
      return null;
    };
  }
}
