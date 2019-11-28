package sonia.scm;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class NotFoundException extends ExceptionWithContext {

  private static final long serialVersionUID = 1710455380886499111L;

  private static final String CODE = "AGR7UzkhA1";

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
    super(context, createMessage(context));
  }

  @Override
  public String getCode() {
    return CODE;
  }

  private static String createMessage(List<ContextEntry> context) {
    return context.stream()
      .map(c -> c.getType().toLowerCase() + " with id " + c.getId())
      .collect(joining(" in ", "could not find ", ""));
  }
}
