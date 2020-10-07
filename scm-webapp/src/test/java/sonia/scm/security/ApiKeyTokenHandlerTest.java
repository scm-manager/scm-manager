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
}
