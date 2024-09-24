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

package sonia.scm.repository;

import jakarta.inject.Inject;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

public class HgRepositoryConfigStore {

  private static final String STORE_NAME = "hgConfig";
  private final ConfigurationStoreFactory factory;

  @Inject
  public HgRepositoryConfigStore(ConfigurationStoreFactory factory) {
    this.factory = factory;
  }

  public HgRepositoryConfig of(Repository repository) {
    ConfigurationStore<HgRepositoryConfig> store = store(repository);
    return store.getOptional().orElse(new HgRepositoryConfig());
  }

  public void store(Repository repository, HgRepositoryConfig config) {
    RepositoryPermissions.custom("hg", repository).check();
    store(repository).set(config);
  }

  private ConfigurationStore<HgRepositoryConfig> store(Repository repository) {
    return factory.withType(HgRepositoryConfig.class)
      .withName(STORE_NAME)
      .forRepository(repository)
      .build();
  }

}
