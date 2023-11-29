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
