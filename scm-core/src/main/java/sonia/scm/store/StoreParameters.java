package sonia.scm.store;

import sonia.scm.repository.Repository;

/**
 * The fields of the {@link StoreParameters} are used from the {@link BlobStoreFactory} to create a store.
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public interface StoreParameters {

  String getName();

  Repository getRepository();
}
