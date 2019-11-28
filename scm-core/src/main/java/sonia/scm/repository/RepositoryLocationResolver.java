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
     * @throws IllegalStateException when there is no known location for the given repository.
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
}
