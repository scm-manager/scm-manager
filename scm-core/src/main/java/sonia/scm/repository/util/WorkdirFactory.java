package sonia.scm.repository.util;

public interface WorkdirFactory<R extends AutoCloseable, C> {
  WorkingCopy<R> createWorkingCopy(C context);
}
