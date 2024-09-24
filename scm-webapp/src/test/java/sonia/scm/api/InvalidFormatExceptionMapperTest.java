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
