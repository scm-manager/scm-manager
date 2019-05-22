package sonia.scm.repository.update;

import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;
import java.util.Optional;

public class MigrationStrategyDao {

  private final RepositoryMigrationPlan plan;
  private final ConfigurationStore<RepositoryMigrationPlan> store;

  @Inject
  public MigrationStrategyDao(ConfigurationStoreFactory storeFactory) {
    store = storeFactory.withType(RepositoryMigrationPlan.class).withName("migration-plan").build();
    this.plan = store.getOptional().orElse(new RepositoryMigrationPlan());
  }

  public Optional<MigrationStrategy> get(String id) {
    return plan.get(id);
  }

  public void set(String repositoryId, MigrationStrategy strategy) {
    plan.set(repositoryId, strategy);
    store.set(plan);
  }
}
