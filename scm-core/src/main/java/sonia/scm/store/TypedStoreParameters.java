package sonia.scm.store;

import sonia.scm.repository.Repository;

/**
 * The fields of the {@link TypedStoreParameters} are used from the {@link ConfigurationStoreFactory},
 * {@link ConfigurationEntryStoreFactory} and {@link DataStoreFactory} to create a type safe store.
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public interface TypedStoreParameters<T> {

  Class<T> getType();

  String getName();

  String getRepositoryId();
}
