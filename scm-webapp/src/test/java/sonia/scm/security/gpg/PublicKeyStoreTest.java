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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.store.InMemoryDataStore;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PublicKeyStoreTest {

  private PublicKeyStore keyStore;

  @BeforeEach
  void setUpKeyStore() {
    keyStore = new PublicKeyStore(new InMemoryDataStore<>(), () -> "trillian");
  }

  @Test
  void shouldReturnStoredKey() throws IOException {
    String rawKey = GPGTestHelper.readKey("single.asc");
    Instant now = Instant.now();

    RawGpgKey key = keyStore.add("SCM Package Key", rawKey);
    assertThat(key.getId()).isEqualTo("0x975922F193B07D6E");
    assertThat(key.getDisplayName()).isEqualTo("SCM Package Key");
    assertThat(key.getOwner()).isEqualTo("trillian");
    assertThat(key.getCreated()).isAfterOrEqualTo(now);
    assertThat(key.getRaw()).isEqualTo(rawKey);
  }

  @Test
  void shouldFindStoredKeyById() throws IOException {
    String rawKey = GPGTestHelper.readKey("single.asc");
    keyStore.add("SCM Package Key", rawKey);
    Optional<RawGpgKey> key = keyStore.findById("0x975922F193B07D6E");
    assertThat(key).isPresent();
  }

}
