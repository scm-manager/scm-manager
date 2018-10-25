package sonia.scm;

import java.util.List;

public interface ExceptionWithContext {
  List<ContextEntry> getContext();

  String getMessage();
}
