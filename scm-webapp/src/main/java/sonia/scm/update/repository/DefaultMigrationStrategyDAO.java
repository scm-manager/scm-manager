package sonia.scm.update.repository;

import sonia.scm.migration.MigrationDAO;
import sonia.scm.migration.MigrationInfo;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
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
