package sonia.scm;

import java.util.List;

import static java.util.Collections.unmodifiableList;

public abstract class ExceptionWithContext extends RuntimeException {

  private final List<ContextEntry> context;

  public ExceptionWithContext(List<ContextEntry> context, String message) {
    super(message);
    this.context = context;
  }

  public ExceptionWithContext(List<ContextEntry> context, String message, Exception cause) {
    super(message, cause);
    this.context = context;
  }

  public List<ContextEntry> getContext() {
    return unmodifiableList(context);
  }

  public abstract String getCode();
}
