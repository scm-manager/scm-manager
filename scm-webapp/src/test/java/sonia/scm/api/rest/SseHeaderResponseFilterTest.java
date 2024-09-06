/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.assertj.core.api.AbstractStringAssert;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.api.v2.CacheControlResponseFilter;
import sonia.scm.sse.SseResponse;
import sonia.scm.web.RestDispatcher;

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
