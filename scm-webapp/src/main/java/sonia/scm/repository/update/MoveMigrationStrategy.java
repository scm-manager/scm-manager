package sonia.scm.repository.update;

import sonia.scm.SCMContextProvider;

import javax.inject.Inject;
import java.nio.file.Path;

class MoveMigrationStrategy implements MigrationStrategy.Instance {

  private final SCMContextProvider contextProvider;

  @Inject
  public MoveMigrationStrategy(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public Path migrate(String id, String name, String type) {
    return null;
  }
}
