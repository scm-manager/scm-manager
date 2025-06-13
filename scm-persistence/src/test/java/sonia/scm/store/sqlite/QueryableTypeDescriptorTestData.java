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

package sonia.scm.store.sqlite;

import sonia.scm.plugin.QueryableTypeDescriptor;
import sonia.scm.store.IdGenerator;

import java.lang.reflect.Constructor;

public class QueryableTypeDescriptorTestData {

  public static QueryableTypeDescriptor createDescriptor(String clazz, String[] t) {
    return createDescriptor("", clazz, t);
  }

  public static QueryableTypeDescriptor createDescriptor(String name, String clazz, String[] t) {
    return createDescriptor(name, clazz, t, IdGenerator.DEFAULT);
  }

  public static QueryableTypeDescriptor createDescriptor(String name, String clazz, String[] t, IdGenerator idGenerator) {
    try {
      Constructor<QueryableTypeDescriptor> constructor = QueryableTypeDescriptor.class
        .getDeclaredConstructor(String.class, String.class, String[].class, IdGenerator.class);
      constructor.setAccessible(true);
      return constructor.newInstance(name, clazz, t, idGenerator);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
