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

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class KeysExtractorTest {

  @Test
  void shouldExtractPublicKeyFromRawKey() throws IOException {
    String raw = GPGTestHelper.readResourceAsString("single.asc");

    PGPPublicKey publicKey = KeysExtractor.extractPublicKey(raw);

    assertThat(publicKey).isNotNull();
    assertThat(Long.toHexString(publicKey.getKeyID())).isEqualTo("975922f193b07d6e");
  }

  @Test
  void shouldExtractPrivateKeyFromRawKey() throws IOException {
    String raw = GPGTestHelper.readResourceAsString("private-key.asc");
    final PGPPrivateKey privateKey = KeysExtractor.extractPrivateKey(raw);
    assertThat(privateKey).isNotNull();
  }

}
