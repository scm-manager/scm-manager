package sonia.scm;

import sonia.scm.repository.Repository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

public class NotFoundException extends RuntimeException {

  private final List<ContextEntry> context;

  public NotFoundException(Class type, String id) {
    this.context = Collections.singletonList(new ContextEntry(type, id));
  }

  public NotFoundException(String type, String id) {
    this.context = Collections.singletonList(new ContextEntry(type, id));
  }

  private NotFoundException(List<ContextEntry> context) {
    this.context = context;
  }

  public static NotFoundExceptionBuilder notFound(Class type, String id) {
    NotFoundExceptionBuilder builder = new NotFoundExceptionBuilder();
    return builder.in(type, id);
  }

  public static NotFoundExceptionBuilder notFound(String type, String id) {
    NotFoundExceptionBuilder builder = new NotFoundExceptionBuilder();
    return builder.in(type, id);
  }

  public List<ContextEntry> getContext() {
    return unmodifiableList(context);
  }

  @Override
  public String getMessage() {
    return context.stream()
      .map(c -> c.getType().toLowerCase() + " with id " + c.getId())
      .collect(joining(" in ", "could not find ", ""));
  }

  public static class NotFoundExceptionBuilder {
    private final List<ContextEntry> context = new LinkedList<>();

    public NotFoundExceptionBuilder in(Repository repository) {
      this.in(Repository.class, repository.getNamespaceAndName().logString());
      return this;
    }

    public NotFoundExceptionBuilder in(Class type, String id) {
      this.context.add(new ContextEntry(type, id));
      return this;
    }

    public NotFoundExceptionBuilder in(String type, String id) {
      this.context.add(new ContextEntry(type, id));
      return this;
    }

    public NotFoundException build() {
      return new NotFoundException(context);
    }
  }

}
