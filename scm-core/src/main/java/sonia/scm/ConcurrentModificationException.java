package sonia.scm;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class ConcurrentModificationException extends ExceptionWithContext {

  private static final String CODE = "2wR7UzpPG1";

  public ConcurrentModificationException(Class type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  public ConcurrentModificationException(String type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  private ConcurrentModificationException(List<ContextEntry> context) {
    super(context, createMessage(context));
  }

  @Override
  public String getCode() {
    return CODE;
  }

  private static String createMessage(List<ContextEntry> context) {
    return context.stream()
      .map(c -> c.getType().toLowerCase() + " with id " + c.getId())
      .collect(joining(" in ", "", " has been modified concurrently"));
  }
}
