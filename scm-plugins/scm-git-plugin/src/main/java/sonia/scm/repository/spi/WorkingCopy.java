package sonia.scm.repository.spi;

import org.eclipse.jgit.lib.Repository;
import sonia.scm.repository.CloseableWrapper;

import java.util.function.Consumer;

public class WorkingCopy extends CloseableWrapper<Repository> {
  WorkingCopy(Repository wrapped, Consumer<Repository> cleanup) {
    super(wrapped, cleanup);
  }
}
