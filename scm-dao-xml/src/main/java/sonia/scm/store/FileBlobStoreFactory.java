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


import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.security.KeyGenerator;
import sonia.scm.util.IOUtil;

import java.io.File;

/**
 * File based store factory.
 *
 */
@Singleton
public class FileBlobStoreFactory extends FileBasedStoreFactory implements BlobStoreFactory {

  private final KeyGenerator keyGenerator;

  @Inject
  public FileBlobStoreFactory(SCMContextProvider contextProvider , RepositoryLocationResolver repositoryLocationResolver, KeyGenerator keyGenerator, RepositoryReadOnlyChecker readOnlyChecker) {
    super(contextProvider, repositoryLocationResolver, Store.BLOB, readOnlyChecker);
    this.keyGenerator = keyGenerator;
  }

  @Override
  @SuppressWarnings("unchecked")
  public BlobStore getStore(StoreParameters storeParameters) {
    File storeLocation = getStoreLocation(storeParameters);
    IOUtil.mkdirs(storeLocation);
    return new FileBlobStore(keyGenerator, storeLocation, mustBeReadOnly(storeParameters));
  }
}
