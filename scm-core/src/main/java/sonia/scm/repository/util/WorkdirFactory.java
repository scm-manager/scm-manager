package sonia.scm.repository.util;

public interface WorkdirFactory<T, C> {
  WorkingCopy<T> createWorkingCopy(C gitContext);
}
