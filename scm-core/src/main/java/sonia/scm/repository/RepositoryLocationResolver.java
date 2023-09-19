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

import java.util.function.BiConsumer;

public abstract class RepositoryLocationResolver {

  public abstract boolean supportsLocationType(Class<?> type);

  protected abstract <T> RepositoryLocationResolverInstance<T> create(Class<T> type);

  public final <T> RepositoryLocationResolverInstance<T> forClass(Class<T> type) {
    if (!supportsLocationType(type)) {
      throw new IllegalStateException("no support for location of class " + type);
    }
    return create(type);
  }

  public interface RepositoryLocationResolverInstance<T> {

    /**
     * Get the existing location for the repository.
     * @param repositoryId The id of the repository.
     * @throws LocationNotFoundException when there is no known location for the given repository.
     */
    T getLocation(String repositoryId);

    /**
     * Create a new location for the new repository.
     * @param repositoryId The id of the new repository.
     * @throws IllegalStateException when there already is a location for the given repository registered.
     */
    T createLocation(String repositoryId);

    /**
     * Set the location of a new repository.
     * @param repositoryId The id of the new repository.
     * @throws IllegalStateException when there already is a location for the given repository registered.
     */
    void setLocation(String repositoryId, T location);

    /**
     * Iterates all repository locations known to this resolver instance and calls the consumer giving the repository id
     * and its location for each repository.
     * @param consumer This callback will be called for each repository with the repository id and its location.
     */
    void forAllLocations(BiConsumer<String, T> consumer);
  }

  public class LocationNotFoundException extends IllegalStateException {
    public LocationNotFoundException(String repositoryId) {
      super("location for repository " + repositoryId + " does not exist");
    }
  }
}
