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

package sonia.scm.security.gpg;

import org.junit.jupiter.api.Test;
import sonia.scm.security.NotPublicKeyException;
import sonia.scm.security.PublicKey;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultPublicKeyParserTest {

  private final DefaultPublicKeyParser keyParser = new DefaultPublicKeyParser();

  @Test
  void shouldParsePublicKey() throws IOException {
    String raw = GPGTestHelper.readResourceAsString("single.asc");
    PublicKey key = keyParser.parse(raw);
    assertThat(key.getId()).isEqualTo("0x975922F193B07D6E");
  }

  @Test
  void shouldParsePublicKeyWithSubkeys() throws IOException {
    String raw = GPGTestHelper.readResourceAsString("subkeys.asc");
    PublicKey key = keyParser.parse(raw);
    assertThat(key.getId()).isEqualTo("0x13B13D4C8A9350A1");
    assertThat(key.getSubkeys()).containsOnly(
      "0x247E908C6FD35473", "0xE50E1DD8B90D3A6B", "0xBF49759E43DD0E60"
    );
  }

  @Test
  void shouldFailForNonPublicKey() {
    String raw = "=== PRIVATE KEY === abcd";
    assertThrows(NotPublicKeyException.class, () -> keyParser.parse(raw));
  }

}
