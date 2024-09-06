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
