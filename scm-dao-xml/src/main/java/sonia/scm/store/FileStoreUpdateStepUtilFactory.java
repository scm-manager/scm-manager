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

import jakarta.inject.Inject;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.StoreUpdateStepUtilFactory;

public class FileStoreUpdateStepUtilFactory implements StoreUpdateStepUtilFactory {

  private final RepositoryLocationResolver locationResolver;
  private final SCMContextProvider contextProvider;

  @Inject
  public FileStoreUpdateStepUtilFactory(RepositoryLocationResolver locationResolver, SCMContextProvider contextProvider) {
    this.locationResolver = locationResolver;
    this.contextProvider = contextProvider;
  }

  @Override
  public StoreUpdateStepUtil build(StoreType type, StoreParameters parameters) {
    return new FileStoreUpdateStepUtil(locationResolver, contextProvider, parameters, type);
  }

}
