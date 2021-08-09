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
import java.util.ArrayList;
import java.util.List;

final class SearchableTypes {

  private SearchableTypes() {
  }

  static LuceneSearchableType create(Class<?> type) {
    List<LuceneSearchableField> fields = new ArrayList<>();
    IndexedType annotation = type.getAnnotation(IndexedType.class);
    if (annotation == null) {
      throw new IllegalArgumentException(
        type.getName() + " has no " + IndexedType.class.getSimpleName() + " annotation"
      );
    }
    collectFields(type, fields);
    return new LuceneSearchableType(type, annotation, fields);
  }

  private static void collectFields(Class<?> type, List<LuceneSearchableField> fields) {
    Class<?> parent = type.getSuperclass();
    if (parent != null) {
      collectFields(parent, fields);
    }
    for (Field field : type.getDeclaredFields()) {
      Indexed indexed = field.getAnnotation(Indexed.class);
      if (indexed != null) {
        fields.add(new LuceneSearchableField(field, indexed));
      }
    }
  }

}
