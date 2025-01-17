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
