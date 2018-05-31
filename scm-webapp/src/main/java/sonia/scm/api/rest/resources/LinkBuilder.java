package sonia.scm.api.rest.resources;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class LinkBuilder {
  private final UriInfo uriInfo;
  private final Class[] classes;
  private final List<Call> calls;

  LinkBuilder(UriInfo uriInfo, Class... classes) {
    this(uriInfo, classes, Collections.emptyList());
  }

  private LinkBuilder(UriInfo uriInfo, Class[] classes, List<Call> calls) {
    this.uriInfo = uriInfo;
    this.classes = classes;
    this.calls = calls;
  }


  public Parameters method(String method) {
    if (calls.size() >= classes.length) {
      throw new IllegalStateException("no more classes for methods");
    }
    return new Parameters(method);
  }

  class Parameters {

    private final String method;

    private Parameters(String method) {
      this.method = method;
    }

    public LinkBuilder parameters(String... parameters) {
      return LinkBuilder.this.add(method, parameters);
    }
  }

  private LinkBuilder add(String method, String[] parameters) {
    List<Call> newCalls = new ArrayList<>(this.calls);
    newCalls.add(new Call(LinkBuilder.this.classes[calls.size()], method, parameters));
    return new LinkBuilder(uriInfo, classes, newCalls);
  }

  public Link create() {
    if (calls.size() < classes.length) {
      throw new IllegalStateException("not enough methods for all classes");
    }

    URI baseUri = uriInfo.getBaseUri();
    URI relativeUri = createRelativeUri();
    URI absoluteUri = baseUri.resolve(relativeUri);
    return new Link(absoluteUri);
  }

  private URI createRelativeUri() {
    UriBuilder uriBuilder = userUriBuilder();
    calls.forEach(call -> uriBuilder.path(call.clazz, call.method));
    String[] concatenatedParameters = calls
      .stream()
      .map(call -> call.parameters)
      .flatMap(Arrays::stream)
      .toArray(String[]::new);
    return uriBuilder.build(concatenatedParameters);
  }

  private UriBuilder userUriBuilder() {
    return UriBuilder.fromResource(classes[0]);
  }

  private static class Call {
    private final Class clazz;
    private final String method;
    private final String[] parameters;

    private Call(Class clazz, String method, String[] parameters) {
      this.clazz = clazz;
      this.method = method;
      this.parameters = parameters;
    }
  }
}
