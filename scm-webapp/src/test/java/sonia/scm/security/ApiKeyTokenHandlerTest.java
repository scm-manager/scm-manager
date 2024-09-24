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

package sonia.scm.security;

import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyTokenHandlerTest {

  ApiKeyTokenHandler handler = new ApiKeyTokenHandler();

  @Test
  void shouldSerializeAndDeserializeToken() {
    String tokenString = handler.createToken("dent", new ApiKey("42", "hg2g", "READ", now()), "some secret");

    Optional<ApiKeyTokenHandler.Token> token = handler.readToken(tokenString);

    assertThat(token).isNotEmpty();
    assertThat(token).get().extracting("user").isEqualTo("dent");
    assertThat(token).get().extracting("apiKeyId").isEqualTo("42");
    assertThat(token).get().extracting("passphrase").isEqualTo("some secret");
  }

  @Test
  void shouldNotFailWithInvalidTokenEncoding() {
    Optional<ApiKeyTokenHandler.Token> token = handler.readToken("invalid token");

    assertThat(token).isEmpty();
  }

  @Test
  void shouldNotFailWithInvalidTokenContent() {
    Optional<ApiKeyTokenHandler.Token> token = handler.readToken(Encoders.BASE64URL.encode("{\"invalid\":\"token\"}".getBytes()));

    assertThat(token).isEmpty();
  }

  @Test
  void shouldParseRealWorldExample() {
    Optional<ApiKeyTokenHandler.Token> token = handler.readToken("eyJhcGlLZXlJZCI6IkE2U0ROWmV0MjEiLCJ1c2VyIjoiaG9yc3QiLCJwYXNzcGhyYXNlIjoiWGNKQ01PMnZuZ1JaOEhVU21BSVoifQ");

    assertThat(token).get().extracting("user").isEqualTo("horst");
  }
}
