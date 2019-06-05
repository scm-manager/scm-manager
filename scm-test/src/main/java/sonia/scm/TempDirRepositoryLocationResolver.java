package sonia.scm;

import sonia.scm.repository.BasicRepositoryLocationResolver;

import java.io.File;
import java.nio.file.Path;

public class TempDirRepositoryLocationResolver extends BasicRepositoryLocationResolver {
  private final File tempDirectory;

  public TempDirRepositoryLocationResolver(File tempDirectory) {
    super(Path.class);
    this.tempDirectory = tempDirectory;
  }

  @Override
  protected <T> RepositoryLocationResolverInstance<T> create(Class<T> type) {
    return new RepositoryLocationResolverInstance<T>() {
      @Override
      public T getLocation(String repositoryId) {
        return (T) tempDirectory.toPath();
      }

      @Override
      public T createLocation(String repositoryId) {
        return (T) tempDirectory.toPath();
      }

      @Override
      public void setLocation(String repositoryId, T location) {
        throw new UnsupportedOperationException("not implemented for tests");
      }
    };
  }
}
