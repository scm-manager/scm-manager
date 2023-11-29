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

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidFormatExceptionMapperTest {

  @Test
  void shouldMapInvalidFormatExceptionDueToInvalidEnum() throws URISyntaxException, UnsupportedEncodingException {
    Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addSingletonResource(new SimpleResource());
    dispatcher.getProviderFactory().registerProvider(InvalidFormatExceptionMapper.class);

    MockHttpRequest request = MockHttpRequest
      .post("/")
      .contentType("application/json")
      .content("{\"e\": \"NONE\"}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(response.getContentAsString()).contains("2qRyyaVcJ1");
  }

  @Path("/")
  static class SimpleResource {
    @POST
    public void post(ObjectWithEnum o) {
    }
  }

  static class ObjectWithEnum {
    public TestEnum e;
  }

  enum TestEnum {
    ONE, TWO
  }
}
