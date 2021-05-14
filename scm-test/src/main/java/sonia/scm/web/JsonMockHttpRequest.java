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

package sonia.scm.web;

import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.specimpl.ResteasyUriInfo;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class JsonMockHttpRequest implements HttpRequest {

  private final MockHttpRequest delegate;
  private boolean contentTypeSet = false;

  private JsonMockHttpRequest(MockHttpRequest delegate) {
    this.delegate = delegate;
  }

  /**
   * @see MockHttpRequest#post(String)
   */
  public static JsonMockHttpRequest post(String url) throws URISyntaxException {
    return new JsonMockHttpRequest(MockHttpRequest.post(url));
  }

  /**
   * @see MockHttpRequest#put(String)
   */
  public static JsonMockHttpRequest put(String url) throws URISyntaxException {
    return new JsonMockHttpRequest(MockHttpRequest.put(url));
  }

  /**
   * @see MockHttpRequest#setHttpMethod(String)
   */
  public void setHttpMethod(String method) {
    delegate.setHttpMethod(method);
  }

  /**
   * @see MockHttpRequest#getAsynchronousContext()
   */
  public ResteasyAsynchronousContext getAsynchronousContext() {
    return delegate.getAsynchronousContext();
  }

  /**
   * @see MockHttpRequest#setAsynchronousContext(ResteasyAsynchronousContext)
   */
  public void setAsynchronousContext(ResteasyAsynchronousContext asynchronousContext) {
    delegate.setAsynchronousContext(asynchronousContext);
  }

  public JsonMockHttpRequest header(String name, String value) {
    delegate.header(name, value);
    return this;
  }

  public JsonMockHttpRequest accept(List<MediaType> accepts) {
      delegate.accept(accepts);
    return this;
  }

  public JsonMockHttpRequest accept(MediaType accept) {
      delegate.accept(accept);
    return this;
  }

  public JsonMockHttpRequest accept(String type) {
      delegate.accept(type);
    return this;
  }

  public JsonMockHttpRequest language(String language) {
      delegate.language(language);
    return this;
  }

  public JsonMockHttpRequest cookie(String name, String value) {
      delegate.cookie(name, value);
    return this;
  }

  public JsonMockHttpRequest contentType(String type) {
    contentTypeSet = true;
      delegate.contentType(type);
    return this;
  }

  public JsonMockHttpRequest contentType(MediaType type) {
    contentTypeSet = true;
      delegate.contentType(type);
    return this;
  }

  public JsonMockHttpRequest content(byte[] bytes) {
      delegate.content(bytes);
    return this;
  }

  public JsonMockHttpRequest json(String json) {
    if (!contentTypeSet) {
      contentType("application/json");
    }
    return content(json.replaceAll("'", "\"").getBytes(UTF_8));
  }

  public JsonMockHttpRequest content(InputStream stream) {
      delegate.content(stream);
    return this;
  }

  public JsonMockHttpRequest addFormHeader(String name, String value) {
      delegate.addFormHeader(name, value);
    return this;
  }

  public HttpHeaders getHttpHeaders() {
    return delegate.getHttpHeaders();
  }

  public MultivaluedMap<String, String> getMutableHeaders() {
    return delegate.getMutableHeaders();
  }

  public InputStream getInputStream() {
    return delegate.getInputStream();
  }

  public void setInputStream(InputStream stream) {
    delegate.setInputStream(stream);
  }

  public ResteasyUriInfo getUri() {
    return delegate.getUri();
  }

  public String getHttpMethod() {
    return delegate.getHttpMethod();
  }

  public void initialRequestThreadFinished() {
    delegate.initialRequestThreadFinished();
  }

  public Object getAttribute(String attribute) {
    return delegate.getAttribute(attribute);
  }

  public void setAttribute(String name, Object value) {
    delegate.setAttribute(name, value);
  }

  public void removeAttribute(String name) {
    delegate.removeAttribute(name);
  }

  public Enumeration<String> getAttributeNames() {
    return delegate.getAttributeNames();
  }

  public ResteasyAsynchronousContext getAsyncContext() {
    return delegate.getAsyncContext();
  }

  public void forward(String path) {
    delegate.forward(path);
  }

  public boolean wasForwarded() {
    return delegate.wasForwarded();
  }

  public String getRemoteHost() {
    return delegate.getRemoteHost();
  }

  public String getRemoteAddress() {
    return delegate.getRemoteAddress();
  }

  public boolean formParametersRead() {
    return delegate.formParametersRead();
  }

  public MultivaluedMap<String, String> getFormParameters() {
    return delegate.getFormParameters();
  }

  public MultivaluedMap<String, String> getDecodedFormParameters() {
    return delegate.getDecodedFormParameters();
  }

  public boolean isInitial() {
    return delegate.isInitial();
  }

  public void setRequestUri(URI requestUri) throws IllegalStateException {
    delegate.setRequestUri(requestUri);
  }

  public void setRequestUri(URI baseUri, URI requestUri) throws IllegalStateException {
    delegate.setRequestUri(baseUri, requestUri);
  }
}
