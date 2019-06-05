package sonia.scm.repository;

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
    T getLocation(String repositoryId);

    void setLocation(String repositoryId, T location);
  }
}
