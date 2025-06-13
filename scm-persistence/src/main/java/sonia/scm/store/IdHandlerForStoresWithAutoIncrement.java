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

package sonia.scm.store;

import java.util.function.BiFunction;

public class IdHandlerForStoresWithAutoIncrement<T> implements IdHandlerForStores<T> {

  private final BiFunction<String, T, String> doPut;

  public IdHandlerForStoresWithAutoIncrement(Class<T> clazz, BiFunction<String, T, String> doPut) {
    checkIdField(clazz);
    this.doPut = doPut;
  }

  private static void checkIdField(Class<?> clazz) {
    new IdFieldFinder().getIdField(clazz)
      .ifPresent(x -> {
        throw new StoreException("The combination of @Id and auto-increment is not supported.");
      });
  }

  public String put(T item) {
    return putWithAutoIncrement(null, item);
  }

  public void put(String id, T item) {
    putWithAutoIncrement(id, item);
  }

  private String putWithAutoIncrement(String id, T item) {
    return doPut.apply(id, item);
  }
}
