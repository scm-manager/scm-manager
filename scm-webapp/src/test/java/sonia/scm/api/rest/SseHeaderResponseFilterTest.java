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

package sonia.scm.api.rest;

import org.assertj.core.api.AbstractStringAssert;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.api.v2.CacheControlResponseFilter;
import sonia.scm.sse.SseResponse;
import sonia.scm.web.RestDispatcher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SseHeaderResponseFilterTest {

  private RestDispatcher restDispatcher;

  @BeforeEach
  void setUp() {
    restDispatcher = new RestDispatcher();
    restDispatcher.getProviderFactory().register(new SseHeaderResponseFilter());
    restDispatcher.addSingletonResource(new FakeSseResource());
  }

  @Test
  void shouldAddResponseHeaders() throws URISyntaxException {
    MockHttpResponse response = invoke("/fake/sse");

    assertThat(response.getStatus()).isEqualTo(200);
    assertContentType(response, MediaType.SERVER_SENT_EVENTS_TYPE);
    assertStringHeader(response, "Cache-Control").isEqualTo("no-cache, no-transform");
    assertStringHeader(response, "X-Accel-Buffering").isEqualTo("no");
  }

  @Test
  void shouldSkipNonSseResponses() throws URISyntaxException {
    MockHttpResponse response = invoke("/fake/non");

    assertThat(response.getStatus()).isEqualTo(200);
    assertContentType(response, MediaType.TEXT_PLAIN_TYPE);
    assertStringHeader(response, "Cache-Control").isNull();
    assertStringHeader(response, "X-Accel-Buffering").isNull();
  }

  @Test
  void shouldAddCacheControlOnlyOnce() throws URISyntaxException {
    restDispatcher.getProviderFactory().register(new CacheControlResponseFilter());
    MockHttpResponse response = invoke("/fake/sse");

    List<String> values = response.getOutputHeaders()
      .get("Cache-Control")
      .stream()
      .map(Object::toString)
      .collect(Collectors.toList());

    assertThat(values).containsOnly("no-cache, no-transform");
  }

  private void assertContentType(MockHttpResponse response, MediaType expected) {
    MediaType contentType = (MediaType) response.getOutputHeaders().getFirst("Content-Type");
    assertThat(contentType.isCompatible(expected)).isTrue();
  }

  private AbstractStringAssert<?> assertStringHeader(MockHttpResponse response, String headerName) {
    Object value = response.getOutputHeaders().getFirst(headerName);
    if (value != null) {
      return assertThat(value.toString());
    }
    return assertThat((String) null);
  }

  private MockHttpResponse invoke(String uri) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get(uri);
    MockHttpResponse response = new MockHttpResponse();
    restDispatcher.invoke(request, response);
    return response;
  }

  @Path("/fake")
  public static class FakeSseResource {

    @GET
    @SseResponse
    @Path("sse")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public String fakeSse() {
      return "sse";
    }

    @GET
    @Path("non")
    @Produces(MediaType.TEXT_PLAIN)
    public String nonSse() {
      return "non-sse";
    }

  }

}
