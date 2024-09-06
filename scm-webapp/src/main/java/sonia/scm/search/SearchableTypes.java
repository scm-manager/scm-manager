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
