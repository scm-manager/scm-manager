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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Encoder;
import io.jsonwebtoken.io.Encoders;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

class ApiKeyTokenHandler {

  private static final Encoder<byte[], String> encoder = Encoders.BASE64URL;
  private static final Decoder<String, byte[]> decoder = Decoders.BASE64URL;
  private static final Logger LOG = LoggerFactory.getLogger(ApiKeyTokenHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  String createToken(String user, ApiKey apiKey, String passphrase) {
    final Token token = new Token(apiKey.getId(), user, passphrase);
    try {
      return encoder.encode(OBJECT_MAPPER.writeValueAsBytes(token));
    } catch (JsonProcessingException e) {
      LOG.error("could not serialize token");
      throw new TokenSerializationException(e);
    }
  }

  Optional<Token> readToken(String token) {
    try {
      return of(OBJECT_MAPPER.readValue(decoder.decode(token), Token.class));
    } catch (IOException | DecodingException e) {
      LOG.debug("failed to read api token, perhaps it is a jwt token or a normal password");
      // do not print the exception here, because it could reveal password details
      return empty();
    }
  }

  @AllArgsConstructor
  @Getter
  public static class Token {
    private final String apiKeyId;
    private final String user;
    private final String passphrase;
  }

  private static class TokenSerializationException extends RuntimeException {
    public TokenSerializationException(Throwable cause) {
      super(cause);
    }
  }
}
