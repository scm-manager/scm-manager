package sonia.scm;

import sonia.scm.repository.Repository;

public class NoChangesMadeException extends BadRequestException {
  public NoChangesMadeException(Repository repository, String branch) {
    super(ContextEntry.ContextBuilder.entity(repository).build(), "no changes detected to branch " + branch);
  }

  public NoChangesMadeException(Repository repository) {
    super(ContextEntry.ContextBuilder.entity(repository).build(), "no changes detected");
  }

  @Override
  public String getCode() {
    return "40RaYIeeR1";
  }
}
