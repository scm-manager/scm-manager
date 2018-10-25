package sonia.scm;

import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

public class NotFoundException extends RuntimeException implements ExceptionWithContext {

  private final List<ContextEntry> context;

  public NotFoundException(Class type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  public NotFoundException(String type, String id) {
    this(Collections.singletonList(new ContextEntry(type, id)));
  }

  public static NotFoundException notFound(ContextEntry.ContextBuilder contextBuilder) {
    return new NotFoundException(contextBuilder.build());
  }

  private NotFoundException(List<ContextEntry> context) {
    super(createMessage(context));
    this.context = context;
  }

  public List<ContextEntry> getContext() {
    return unmodifiableList(context);
  }

  private static String createMessage(List<ContextEntry> context) {
    return context.stream()
      .map(c -> c.getType().toLowerCase() + " with id " + c.getId())
      .collect(joining(" in ", "could not find ", ""));
  }
}
