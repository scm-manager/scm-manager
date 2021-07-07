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

import org.apache.lucene.index.IndexableField;

import javax.annotation.Nonnull;
import java.time.Instant;

class ValueExtractors {

  private ValueExtractors() {
  }

  static ValueExtractor create(String name, Class<?> type) {
    if (Reflection.isString(type)) {
      return stringExtractor(name);
    } else if (Reflection.isLong(type)) {
      return longExtractor(name);
    } else if (Reflection.isInteger(type)) {
      return integerExtractor(name);
    } else if (Reflection.isBoolean(type)) {
      return booleanExtractor(name);
    } else if (Reflection.isInstant(type)) {
      return instantExtractor(name);
    } else {
      throw new UnsupportedTypeOfFieldException(type + " is currently not supported");
    }
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
