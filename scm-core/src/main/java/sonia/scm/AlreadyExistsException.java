package sonia.scm;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

public class AlreadyExistsException extends ExceptionWithContext {

  private static final String CODE = "FtR7UznKU1";

  public AlreadyExistsException(ModelObject object) {
    this(singletonList(new ContextEntry(object.getClass(), object.getId())));
  }

  public static AlreadyExistsException alreadyExists(ContextEntry.ContextBuilder builder) {
    return new AlreadyExistsException(builder.build());
  }

  private AlreadyExistsException(List<ContextEntry> context) {
    super(context, createMessage(context));
  }

  @Override
  public String getCode() {
    return CODE;
  }

  private static String createMessage(List<ContextEntry> context) {
    return context.stream()
      .map(c -> c.getType().toLowerCase() + " with id " + c.getId())
      .collect(joining(" in ", "", " already exists"));
  }
}
