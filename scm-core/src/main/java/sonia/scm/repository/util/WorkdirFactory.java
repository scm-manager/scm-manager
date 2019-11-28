package sonia.scm.repository.util;

public interface WorkdirFactory<R, W, C> {
  WorkingCopy<R, W> createWorkingCopy(C context, String initialBranch);
}
