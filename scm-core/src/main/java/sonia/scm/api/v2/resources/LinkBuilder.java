/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableList;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.Arrays;

/**
 * This class is used to create links for JAX-RS resources. Create a new instance specifying all resource classes used
 * to process the request. Then for each of these classes call <code>builder.method(...).parameters(...)</code> for each
 * of these classes consecutively. The builder itself is immutable, so that each instance is reusable and you get a new
 * builder for each method.
 *
 * <pre>
 * LinkBuilder builder = new LinkBuilder(pathInfo, MainResource.class, SubResource.class);
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
  private final ScmPathInfo pathInfo;
  private final Class[] classes;
  private final ImmutableList<Call> calls;

  public LinkBuilder(ScmPathInfo pathInfo, Class... classes) {
    this(pathInfo, classes, ImmutableList.of());
  }

  private LinkBuilder(ScmPathInfo pathInfo, Class[] classes, ImmutableList<Call> calls) {
    this.pathInfo = pathInfo;
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

    URI baseUri = pathInfo.getApiRestUri();
    URI relativeUri = createRelativeUri();
    return baseUri.resolve(relativeUri);
  }

  public String href() {
    return create().toString();
  }

  private LinkBuilder add(String method, String[] parameters) {
    return new LinkBuilder(pathInfo, classes, appendNewCall(method, parameters));
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
