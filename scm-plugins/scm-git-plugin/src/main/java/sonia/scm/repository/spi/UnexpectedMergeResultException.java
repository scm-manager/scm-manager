package sonia.scm.repository.spi;

import org.eclipse.jgit.api.MergeResult;
import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;
import sonia.scm.repository.Repository;

class UnexpectedMergeResultException extends ExceptionWithContext {

  public static final String CODE = "4GRrgkSC01";

  public UnexpectedMergeResultException(Repository repository, MergeResult result) {
    super(ContextEntry.ContextBuilder.entity(repository).build(), createMessage(result));
  }

  private static String createMessage(MergeResult result) {
    return "unexpected merge result: " + result
      + "\nconflicts: " + result.getConflicts()
      + "\ncheckout conflicts: " + result.getCheckoutConflicts()
      + "\nfailing paths: " + result.getFailingPaths();
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
