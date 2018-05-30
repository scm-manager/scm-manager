package sonia.scm.api.rest.resources;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.*;

class LinkMapBuilder {
  private final UriInfo uriInfo;
  private final Class[] classes;
  private final Map<String, Link> links = new LinkedHashMap<>();

  LinkMapBuilder(UriInfo uriInfo, Class... classes) {
    this.uriInfo = uriInfo;
    this.classes = classes;
  }

  Builder add(String linkName) {
    return new ConcreteBuilder(linkName);
  }

  interface Builder {
    Parameters method(String method);
  }

  interface Parameters {
    Builder parameters(String... parameters);
  }

  private class ConcreteBuilder implements Builder {

    private final String linkName;
    private final List<Call> calls = new LinkedList<>();

    private int callCount = 0;

    ConcreteBuilder(String linkName) {
      this.linkName = linkName;
    }

    public Parameters method(String method) {
      return new ParametersImpl(method);
    }

    private class ParametersImpl implements Parameters {

      private final String method;

      ParametersImpl(String method) {
        this.method = method;
      }

      public Builder parameters(String... parameters) {
        return ConcreteBuilder.this.add(method, parameters);
      }
    }

    private Builder add(String method, String[] parameters) {
      this.calls.add(new Call(LinkMapBuilder.this.classes[callCount], method, parameters));
      ++callCount;
      if (callCount >= classes.length) {
        links.put(linkName, createLink());
        return x -> {
          throw new IllegalStateException("no more classes for methods");
        };
      }
      return this;
    }

    private Link createLink() {
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
  }

  Map<String, Link> getLinkMap() {
    return Collections.unmodifiableMap(links);
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
