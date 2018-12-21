package sonia.scm.api.v2.resources;

import sonia.scm.event.Event;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.Repository;

@Event
public class GitRepositoryConfigChangedEvent {
  private final Repository repository;
  private final GitRepositoryConfig oldConfig;
  private final GitRepositoryConfig newConfig;

  public GitRepositoryConfigChangedEvent(Repository repository, GitRepositoryConfig oldConfig, GitRepositoryConfig newConfig) {
    this.repository = repository;
    this.oldConfig = oldConfig;
    this.newConfig = newConfig;
  }

  public Repository getRepository() {
    return repository;
  }

  public GitRepositoryConfig getOldConfig() {
    return oldConfig;
  }

  public GitRepositoryConfig getNewConfig() {
    return newConfig;
  }
}
