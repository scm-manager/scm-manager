package sonia.scm.repository.util;

public interface WorkdirFactory<R, C> {
  WorkingCopy<R> createWorkingCopy(C context, String initialBranch);
}
