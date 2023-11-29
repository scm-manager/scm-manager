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

package sonia.scm.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.util.Providers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.Failure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FailureExceptionMapperTest {

  @Mock
  private HttpServletRequest request;

  private Dispatcher dispatcher;

  @BeforeEach
  void setUpDispatcher() {
    dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addSingletonResource(new GreetingResource());

    FailureExceptionMapper mapper = new FailureExceptionMapper(Providers.of(request));
    dispatcher.getProviderFactory().registerProviderInstance(mapper);
    dispatcher.getProviderFactory().registerProvider(FallbackExceptionMapper.class);
  }

  @Test
  void shouldReturn200ForOptionsRequests() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.options("/");
    MockHttpResponse response = dispatch(request);

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void shouldKeepStatusCodeFromFailure() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.head("/");
    MockHttpResponse response = dispatch(request);

    assertThat(response.getStatus()).isEqualTo(418);
    assertThat(response.getOutput()).isEmpty();
  }

  @Test
  void shouldAppendErrorDtoForMethodsWithBody() throws URISyntaxException, IOException {
    MockHttpRequest request = MockHttpRequest.post("/");
    MockHttpResponse response = dispatch(request);

    assertThat(response.getStatus()).isEqualTo(418);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(response.getOutput());
    assertThat(node.get("errorCode").asText()).isEqualTo(FailureExceptionMapper.ERROR_CODE);
  }

  private MockHttpResponse dispatch(MockHttpRequest request) {
    lenient().when(this.request.getMethod()).thenReturn(request.getHttpMethod());
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }


  @Path("/")
  public static class GreetingResource {
    @GET
    public String greetings() {
      return "Hello";
    }

    @HEAD
    public Response head() {
      throw new Failure(418);
    }

    @POST
    public Response post() {
      Failure failure = new Failure(418);
      failure.setLoggable(true);
      throw failure;
    }
  }

}
