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

/**
 * The BlobStoreFactory can be used to create a new or get an existing {@link BlobStore}s.
 * <br>
 * You can either create a global {@link BlobStore} or a {@link BlobStore} for a specific repository. To create a global
 * {@link BlobStore} call:
 * <code><pre>
 *     blobStoreFactory
 *       .withName("name")
 *       .build();
 * </pre></code>
 * To create a {@link BlobStore} for a specific repository call:
 * <code><pre>
 *     blobStoreFactory
 *       .withName("name")
 *       .forRepository(repository)
 *       .build();
 * </pre></code>
 *
 * @since 1.23
 *
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.store.BlobStore
 */
public interface BlobStoreFactory {

  /**
   * Creates a new or gets an existing {@link BlobStore}. Instead of calling this method you should use the floating API
   * from {@link #withName(String)}.
   *
   * @param storeParameters The parameters for the blob store.
   * @return A new or an existing {@link BlobStore} for the given parameters.
   */
  BlobStore getStore(final StoreParameters storeParameters);

  /**
   * Use this to create a new or get an existing {@link BlobStore} with a floating API.
   * @param name The name for the {@link BlobStore}.
   * @return Floating API to either specify a repository or directly build a global {@link BlobStore}.
   */
  default StoreParametersBuilder<BlobStore> withName(String name) {
    return new StoreParametersBuilder<>(name, this::getStore);
  }
}
