package sonia.scm.repository.spi;

import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;
import sonia.scm.repository.Repository;

public class CannotDeleteDefaultBranchException extends ExceptionWithContext {

  public static final String CODE = "78RhWxTIw1";

  public CannotDeleteDefaultBranchException(Repository repository, String branchName) {
    super(ContextEntry.ContextBuilder.entity("Branch", branchName).in(repository).build(), "default branch cannot be deleted");
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
