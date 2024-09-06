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

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPublicKeyTest {

  @Test
  void shouldVerifyPublicKey() throws IOException {
    String rawPublicKey = GPGTestHelper.readResourceAsString("subkeys.asc");
    DefaultPublicKey publicKey = new DefaultPublicKey("1", "trillian", rawPublicKey, Collections.emptySet());

    byte[] content = GPGTestHelper.readResourceAsBytes("slarti.txt");
    byte[] signature = GPGTestHelper.readResourceAsBytes("slarti.txt.asc");

    boolean verified = publicKey.verify(content, signature);
    assertThat(verified).isTrue();
  }

}
