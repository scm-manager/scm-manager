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

package sonia.scm.update.repository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.migration.MigrationDAO;
import sonia.scm.migration.MigrationInfo;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Singleton
public class DefaultMigrationStrategyDAO implements MigrationDAO {

  private final RepositoryMigrationPlan plan;
  private final ConfigurationStore<RepositoryMigrationPlan> store;

  @Inject
  public DefaultMigrationStrategyDAO(ConfigurationStoreFactory storeFactory) {
    store = storeFactory.withType(RepositoryMigrationPlan.class).withName("migration-plan").build();
    this.plan = store.getOptional().orElse(new RepositoryMigrationPlan());
  }

  public Optional<RepositoryMigrationPlan.RepositoryMigrationEntry> get(String id) {
    return plan.get(id);
  }

  public void set(String repositoryId, String protocol, String originalName, MigrationStrategy strategy, String newNamespace, String newName) {
    plan.set(repositoryId, protocol, originalName, strategy, newNamespace, newName);
    store.set(plan);
  }

  @Override
  public Collection<MigrationInfo> getAll() {
    return plan
      .getEntries()
      .stream()
      .map(e -> new MigrationInfo(e.getRepositoryId(), e.getProtocol(), e.getOriginalName(), e.getNewNamespace(), e.getNewName()))
      .collect(toList());
  }
}
