package sonia.scm.update.repository;

import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class MigrationStrategyDao {

  private final RepositoryMigrationPlan plan;
  private final ConfigurationStore<RepositoryMigrationPlan> store;

  @Inject
  public MigrationStrategyDao(ConfigurationStoreFactory storeFactory) {
    store = storeFactory.withType(RepositoryMigrationPlan.class).withName("migration-plan").build();
    this.plan = store.getOptional().orElse(new RepositoryMigrationPlan());
  }

  public Optional<RepositoryMigrationPlan.RepositoryMigrationEntry> get(String id) {
    return plan.get(id);
  }

  public void set(String repositoryId, String originalName, MigrationStrategy strategy, String newNamespace, String newName) {
    plan.set(repositoryId, originalName, strategy, newNamespace, newName);
    store.set(plan);
  }
}
