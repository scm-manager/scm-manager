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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

final class Ids {

  private Ids() {
  }

  static Map<String,String> others(Id<?> id) {
    Map<String,String> result = new TreeMap<>();

    Map<Class<?>, String> others = id.getOthers();
    for (Map.Entry<Class<?>, String> e : others.entrySet()) {
      result.put(name(e.getKey()), e.getValue());
    }

    return result;
  }

  static String id(String mainId, Map<String,String> others) {
    List<String> values = new ArrayList<>();
    values.add(mainId);
    values.addAll(others.values());
    return values.stream()
      .map(v -> v.replace(";", "\\;" ))
      .collect(Collectors.joining(";"));
  }

  static String asString(Id<?> id) {
    return id(id.getMainId(), others(id));
  }

  private static String name(Class<?> key) {
    return Names.create(key, key.getAnnotation(IndexedType.class));
  }

}
