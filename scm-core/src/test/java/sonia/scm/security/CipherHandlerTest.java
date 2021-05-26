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
