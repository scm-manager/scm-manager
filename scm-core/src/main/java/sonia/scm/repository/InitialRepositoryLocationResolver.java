/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
 * @author Mohamed Karray
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
   *
   * @param repositoryId id of the repository
   *
   * @return initial path of repository
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
