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

package sonia.scm.repository;

import com.google.common.base.CharMatcher;

import jakarta.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A Location Resolver for File based Repository Storage.
 * <p>
 * <b>WARNING:</b> The Locations provided with this class may not be used from the plugins to store any plugin specific files.
 * <p>
 * Please use the {@link sonia.scm.store.DataStoreFactory } and the {@link sonia.scm.store.DataStore} classes to store data<br>
 * Please use the {@link sonia.scm.store.BlobStoreFactory } and the {@link sonia.scm.store.BlobStore} classes to store binary files<br>
 * Please use the {@link sonia.scm.store.ConfigurationStoreFactory} and the {@link sonia.scm.store.ConfigurationStore} classes  to store configurations
 *
 * @since 2.0.0
 */
public class InitialRepositoryLocationResolver {

  private static final String DEFAULT_REPOSITORY_PATH = "repositories";

  private static final CharMatcher ID_MATCHER = CharMatcher.anyOf("/\\.");

  private final Set<RepositoryLocationOverride> repositoryLocationOverrides;

  @Inject
  public InitialRepositoryLocationResolver(Set<RepositoryLocationOverride> repositoryLocationOverrides) {
    this.repositoryLocationOverrides = repositoryLocationOverrides;
  }

  /**
   * Returns the initial path to repository.
   */
  @SuppressWarnings("squid:S2083") // path traversal is prevented with ID_MATCHER
  public Path getPath(String repositoryId) {
    // avoid path traversal attacks
    checkArgument(ID_MATCHER.matchesNoneOf(repositoryId), "repository id contains invalid characters");
    return Paths.get(DEFAULT_REPOSITORY_PATH, repositoryId);
  }

  public Path getPath(Repository repository) {
    Path path = getPath(repository.getId());
    for (RepositoryLocationOverride o : repositoryLocationOverrides) {
      path = o.overrideLocation(repository, path);
    }
    return path;
  }
}
