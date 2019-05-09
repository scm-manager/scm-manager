package sonia.scm;

import sonia.scm.repository.BasicRepositoryLocationResolver;
import sonia.scm.repository.RepositoryLocationResolver;

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
    return repositoryId -> (T) tempDirectory.toPath();
  }
}
