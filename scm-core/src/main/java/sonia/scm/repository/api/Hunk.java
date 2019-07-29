package sonia.scm.repository.api;

public interface Hunk extends Iterable<DiffLine> {

  int getOldStart();

  int getOldLineCount();

  int getNewStart();

  int getNewLineCount();
}
