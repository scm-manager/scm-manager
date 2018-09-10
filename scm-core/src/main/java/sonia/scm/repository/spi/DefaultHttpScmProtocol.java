package sonia.scm.repository.spi;

import sonia.scm.repository.Repository;

public abstract class DefaultHttpScmProtocol implements HttpScmProtocol {

  private final Repository repository;

  protected DefaultHttpScmProtocol(Repository repository) {
    this.repository = repository;
  }
}
