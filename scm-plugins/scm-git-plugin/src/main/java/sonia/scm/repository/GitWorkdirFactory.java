package sonia.scm.repository;

import sonia.scm.repository.spi.GitContext;
import sonia.scm.repository.spi.WorkingCopy;

public interface GitWorkdirFactory {
  WorkingCopy createWorkingCopy(GitContext gitContext);
}
