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
import jakarta.annotation.Nullable;

final class Names {

  private Names() {
  }

  static String create(Class<?> type) {
    return create(type, type.getAnnotation(IndexedType.class));
  }

  static String create(Class<?> type, @Nullable IndexedType annotation) {
    String nameFromAnnotation = null;
    if (annotation != null) {
      nameFromAnnotation = annotation.value();
    }
    if (Strings.isNullOrEmpty(nameFromAnnotation)) {
      String simpleName = type.getSimpleName();
      return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
    return nameFromAnnotation;
  }

  public static String field(Class<?> type) {
    return "_" + create(type);
  }
}
