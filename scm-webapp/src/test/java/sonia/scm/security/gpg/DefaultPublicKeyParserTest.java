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
