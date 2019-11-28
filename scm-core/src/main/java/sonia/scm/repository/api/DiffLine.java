package sonia.scm.repository.api;

import java.util.OptionalInt;

public interface DiffLine {

  OptionalInt getOldLineNumber();

  OptionalInt getNewLineNumber();

  String getContent();
}
