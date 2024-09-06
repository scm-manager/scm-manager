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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import java.util.Optional;

@Singleton
public class DefaultIndexLogStore implements IndexLogStore {

  private final DataStore<IndexLog> dataStore;

  @Inject
  public DefaultIndexLogStore(DataStoreFactory dataStoreFactory) {
    this.dataStore = dataStoreFactory.withType(IndexLog.class).withName("index-log").build();
  }

  @Override
  public ForIndex forIndex(String index) {
    return new DefaultForIndex(index);
  }

  @Override
  public ForIndex defaultIndex() {
    // constant
    return new DefaultForIndex("default");
  }

  class DefaultForIndex implements ForIndex {

    private final String index;

    private DefaultForIndex(String index) {
      this.index = index;
    }

    private String id(Class<?> type) {
      return index + "_" + type.getName();
    }

    @Override
    public void log(Class<?> type, int version) {
      dataStore.put(id(type), new IndexLog(version));
    }

    @Override
    public Optional<IndexLog> get(Class<?> type) {
      return dataStore.getOptional(id(type));
    }
  }
}
