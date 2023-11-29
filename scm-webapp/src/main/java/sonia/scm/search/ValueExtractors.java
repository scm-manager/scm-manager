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
