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

import sonia.scm.repository.Repository;


public abstract class ConfigurationEntryStoreTestBase extends KeyValueStoreTestBase {


  protected abstract ConfigurationEntryStoreFactory createConfigurationStoreFactory();

  @Override
  protected <T> ConfigurationEntryStore<T> getDataStore(Class<T> type) {
    return this.createConfigurationStoreFactory()
      .withType(type)
      .withName(storeName)
      .build();
  }

  @Override
  protected <T> ConfigurationEntryStore<T> getDataStore(Class<T> type, Repository repository) {
    return this.createConfigurationStoreFactory()
      .withType(type)
      .withName(repoStoreName)
      .forRepository(repository)
      .build();
  }
}
