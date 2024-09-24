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

package sonia.scm.web.lfs;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.repository.Repository;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;

/**
 * Creates {@link BlobStore} objects to store lfs objects.
 *
 * @since 1.54
 */
@Singleton
public class LfsBlobStoreFactory {

  private static final String GIT_LFS_STORE_NAME = "git-lfs";

  private final BlobStoreFactory blobStoreFactory;

  @Inject
  public LfsBlobStoreFactory(BlobStoreFactory blobStoreFactory) {
    this.blobStoreFactory = blobStoreFactory;
  }

  /**
   * Provides a {@link BlobStore} corresponding to the SCM Repository.
   *
   * @param repository The SCM Repository to provide a LFS {@link BlobStore} for.
   *
   * @return blob store for the corresponding scm repository
   */
  public BlobStore getLfsBlobStore(Repository repository) {
    return blobStoreFactory
        .withName(GIT_LFS_STORE_NAME)
        .forRepository(repository)
        .build();
  }
}
