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

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class CipherHandlerTest {

  public static final String SECRET_TEXT = "secret text";
  public static final String SECRET_TEXT_AS_BASE64 = "c2VjcmV0IHRleHQ=";
  public static final String ENCRYPTED_TEXT = "unreadable bytes";

  @Test
  void shouldDelegateToStringEncryptionForBytes() {
    CipherHandler cipherHandler = new CipherHandler() {
      @Override
      public String decode(String value) {
        if (value.equals(ENCRYPTED_TEXT)) {
          return SECRET_TEXT_AS_BASE64;
        } else {
          throw new IllegalArgumentException("unexpected data: " + value);
        }
      }

      @Override
      public String encode(String value) {
        if (value.equals(SECRET_TEXT_AS_BASE64)) {
          return ENCRYPTED_TEXT;
        } else {
          throw new IllegalArgumentException("unexpected data: " + value);
        }
      }
    };

    byte[] encodedBytes = cipherHandler.encode(SECRET_TEXT.getBytes(UTF_8));

    byte[] originalBytes = cipherHandler.decode(encodedBytes);

    assertThat(originalBytes).isEqualTo(SECRET_TEXT.getBytes(UTF_8));
  }
}
