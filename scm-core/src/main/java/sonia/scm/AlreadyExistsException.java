package sonia.scm;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

public class AlreadyExistsException extends RuntimeException implements ExceptionWithContext {

  private static final String CODE = "FtR7UznKU1";

  private final List<ContextEntry> context;

  public AlreadyExistsException(ModelObject object) {
    this(singletonList(new ContextEntry(object.getClass(), object.getId())));
  }

  public static AlreadyExistsException alreadyExists(ContextEntry.ContextBuilder builder) {
    return new AlreadyExistsException(builder.build());
  }

  private AlreadyExistsException(List<ContextEntry> context) {
    super(createMessage(context));
    this.context = context;
  }

  public List<ContextEntry> getContext() {
    return unmodifiableList(context);
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
