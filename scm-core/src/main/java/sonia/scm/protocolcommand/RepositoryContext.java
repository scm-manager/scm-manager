package sonia.scm.protocolcommand;

import sonia.scm.repository.Repository;

import java.nio.file.Path;

public class RepositoryContext {
  private Repository repository;
  private Path directory;

  public RepositoryContext(Repository repository, Path directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public Repository getRepository() {
    return repository;
  }

  public Path getDirectory() {
    return directory;
  }
}
