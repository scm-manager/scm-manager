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
