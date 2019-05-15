package sonia.scm.repository;

public abstract class BasicRepositoryLocationResolver<T> extends RepositoryLocationResolver {

  private final Class<T> type;

  protected BasicRepositoryLocationResolver(Class<T> type) {
    this.type = type;
  }

  @Override
  public boolean supportsLocationType(Class<?> type) {
    return type.isAssignableFrom(this.type);
  }
}
