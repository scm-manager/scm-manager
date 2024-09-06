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

package sonia.scm.api.v2;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.WebApplicationExceptionMapper;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvalidAcceptHeaderFilterTest {

  @Mock
  private ContainerRequestContext requestContext;

  @Mock
  private ContainerResponseContext responseContext;

  @Captor
  private ArgumentCaptor<Object> entityCaptor;

  private final InvalidAcceptHeaderFilter filter = new InvalidAcceptHeaderFilter();

  @Test
  void shouldIgnoreWithOtherStatusCode() {
    when(responseContext.getStatus()).thenReturn(HttpServletResponse.SC_NOT_FOUND);
    filter.filter(requestContext, responseContext);
    verify(responseContext, never()).setEntity(any());
  }

  @Test
  void shouldIgnoreWithoutErrorDto() {
    when(responseContext.getStatus()).thenReturn(HttpServletResponse.SC_NOT_ACCEPTABLE);
    when(responseContext.getEntity()).thenReturn("Hello");
    filter.filter(requestContext, responseContext);
    verify(responseContext, never()).setEntity(any());
  }

  @Test
  void shouldIgnoreWithoutResteasyError() {
    when(responseContext.getStatus()).thenReturn(HttpServletResponse.SC_NOT_ACCEPTABLE);
    ErrorDto dto = new ErrorDto();
    dto.setMessage("Other");
    when(responseContext.getEntity()).thenReturn(dto);
    filter.filter(requestContext, responseContext);
    verify(responseContext, never()).setEntity(any());
  }

  @Nested
  class NoMatchForAcceptHeader {

    @BeforeEach
    void setStatusCode() {
      when(responseContext.getStatus()).thenReturn(HttpServletResponse.SC_NOT_ACCEPTABLE);

      ErrorDto error = new ErrorDto();
      error.setMessage("RESTEASY003635: No match for accept header");

      when(responseContext.getEntity()).thenReturn(error);
    }

    @Test
    void shouldNotModifyResponse() {
      mediaTypes();

      filter.filter(requestContext, responseContext);

      verify(responseContext, never()).setEntity(any());
    }

    @Test
    void shouldNotModifyResponseWithFullQualifiedAccept() {
      mediaTypes(VndMediaType.ANNOTATE);

      filter.filter(requestContext, responseContext);

      verify(responseContext, never()).setEntity(any());
    }

    @Test
    void shouldNotModifyResponseWithMultipleFullQualifiedAccept() {
      mediaTypes(VndMediaType.ANNOTATE, VndMediaType.SOURCE, VndMediaType.BRANCH);

      filter.filter(requestContext, responseContext);

      verify(responseContext, never()).setEntity(any());
    }

    @Test
    void shouldReturnErrorForPartialWildcard() {
      mediaTypes("application/*+json");

      filter.filter(requestContext, responseContext);

      assertError(InvalidAcceptHeaderFilter.CODE_PARTIAL_WILDCARD);
    }

    @Test
    void shouldReturnApplicationJsonError() {
      mediaTypes("application/json");

      filter.filter(requestContext, responseContext);

      assertError(InvalidAcceptHeaderFilter.CODE_APPLICATION_JSON);
    }

  }

  @Nested
  class Integration {

    private Dispatcher dispatcher;

    @BeforeEach
    void prepare() {
      dispatcher = MockDispatcherFactory.createDispatcher();
      dispatcher.getProviderFactory().registerProvider(InvalidAcceptHeaderFilter.class);
      dispatcher.getProviderFactory().registerProvider(WebApplicationExceptionMapper.class);
      dispatcher.getRegistry().addSingletonResource(new SampleResource());
    }

    @Test
    void shouldReturnApplicationJsonError() throws URISyntaxException, UnsupportedEncodingException {
      MockHttpRequest request = MockHttpRequest.get("/").accept("application/json");
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(response.getContentAsString()).contains(InvalidAcceptHeaderFilter.CODE_APPLICATION_JSON);
    }

    @Test
    void shouldReturnErrorForPartialWildcard() throws URISyntaxException, UnsupportedEncodingException {
      MockHttpRequest request = MockHttpRequest.get("/").accept("application/*+json");
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(response.getContentAsString()).contains(InvalidAcceptHeaderFilter.CODE_PARTIAL_WILDCARD);
    }

  }

  @Path("/")
  class SampleResource {

    @GET
    @Produces("text/plain")
    public String hello() {
      return "hello";
    }

  }

  private void assertError(String code) {
    verify(responseContext).setEntity(entityCaptor.capture());
    Object value = entityCaptor.getValue();
    assertThat(value).isInstanceOfSatisfying(ErrorDto.class, error -> {
      assertThat(error.getErrorCode()).isEqualTo(code);
    });
  }

  private void mediaTypes(String... types) {
    List<MediaType> mediaTypes = Arrays.stream(types)
      .map(MediaType::valueOf).collect(Collectors.toList());

    when(requestContext.getAcceptableMediaTypes()).thenReturn(mediaTypes);
  }
}
