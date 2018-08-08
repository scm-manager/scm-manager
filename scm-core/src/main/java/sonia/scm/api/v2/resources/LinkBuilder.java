package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableList;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;

/**
 * This class is used to create links for JAX-RS resources. Create a new instance specifying all resource classes used
 * to process the request. Then for each of these classes call <code>builder.method(...).parameters(...)</code> for each
 * of these classes consecutively. The builder itself is immutable, so that each instance is reusable and you get a new
 * builder for each method.
 *
 * <pre>
 * LinkBuilder builder = new LinkBuilder(uriInfo, MainResource.class, SubResource.class);
 * Link link = builder
 *     .method("sub")
 *     .parameters("param")
 *     .method("x")
 *     .parameters("param_1", "param_2")
 *     .create();
 * </pre>
 */
@SuppressWarnings("WeakerAccess") // Non-public will result in IllegalAccessError for plugins
public class LinkBuilder {
  private final UriInfo uriInfo;
  private final Class[] classes;
  private final ImmutableList<Call> calls;

  public LinkBuilder(UriInfo uriInfo, Class... classes) {
    this(uriInfo, classes, ImmutableList.of());
  }

  private LinkBuilder(UriInfo uriInfo, Class[] classes, ImmutableList<Call> calls) {
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

  public URI create() {
    if (calls.size() < classes.length) {
      throw new IllegalStateException("not enough methods for all classes");
    }

    URI baseUri = uriInfo.getBaseUri();
    URI relativeUri = createRelativeUri();
    return baseUri.resolve(relativeUri);
  }

  public String href() {
    return create().toString();
  }

  private LinkBuilder add(String method, String[] parameters) {
    return new LinkBuilder(uriInfo, classes, appendNewCall(method, parameters));
  }

  private ImmutableList<Call> appendNewCall(String method, String[] parameters) {
    return ImmutableList.<Call> builder().addAll(calls).add(createNewCall(method, parameters)).build();
  }

  private Call createNewCall(String method, String[] parameters) {
    return new Call(LinkBuilder.this.classes[calls.size()], method, parameters);
  }

  private URI createRelativeUri() {
    UriBuilder uriBuilder = userUriBuilder();
    calls.forEach(call -> uriBuilder.path(call.clazz, call.method));
    String[] concatenatedParameters = calls
      .stream()
      .map(call -> call.parameters)
      .flatMap(Arrays::stream)
      .toArray(String[]::new);
    return uriBuilder.build((Object[]) concatenatedParameters);
  }

  private UriBuilder userUriBuilder() {
    return UriBuilder.fromResource(classes[0]);
  }

  public class Parameters {

    private final String method;

    private Parameters(String method) {
      this.method = method;
    }

    public LinkBuilder parameters(String... parameters) {
      return LinkBuilder.this.add(method, parameters);
    }
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
