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
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

final class TypeConverter {

  private final List<FieldConverter> fieldConverters;

  TypeConverter(List<FieldConverter> fieldConverters) {
    this.fieldConverters = fieldConverters;
  }

  public Document convert(Object object) {
    try {
      return doConversion(object);
    } catch (IllegalAccessException | InvocationTargetException ex) {
      throw new SearchEngineException("failed to create document", ex);
    }
  }

  @Nonnull
  private Document doConversion(Object object) throws IllegalAccessException, InvocationTargetException {
    Document document = new Document();
    for (FieldConverter fieldConverter : fieldConverters) {
      for (IndexableField field : fieldConverter.convert(object)) {
        document.add(field);
      }
    }
    return document;
  }

}
