package sonia.scm;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

public class ConcurrentModificationException extends RuntimeException implements ExceptionWithContext {
  private final List<ContextEntry> context;

  public ConcurrentModificationException(Class type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  public ConcurrentModificationException(String type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  private ConcurrentModificationException(List<ContextEntry> context) {
    super(createMessage(context));
    this.context = context;
  }

  public List<ContextEntry> getContext() {
    return unmodifiableList(context);
  }

  private static String createMessage(List<ContextEntry> context) {
    return context.stream()
      .map(c -> c.getType().toLowerCase() + " with id " + c.getId())
      .collect(joining(" in ", "", " has been modified concurrently"));
  }
}
