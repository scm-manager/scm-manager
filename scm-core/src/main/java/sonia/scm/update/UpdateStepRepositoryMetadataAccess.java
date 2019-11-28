package sonia.scm.update;

import sonia.scm.repository.Repository;

/**
 * Use this in {@link sonia.scm.migration.UpdateStep}s only to read repository objects directly from locations given by
 * {@link sonia.scm.repository.RepositoryLocationResolver}.
 */
public interface UpdateStepRepositoryMetadataAccess<T> {
  Repository read(T location);
}
