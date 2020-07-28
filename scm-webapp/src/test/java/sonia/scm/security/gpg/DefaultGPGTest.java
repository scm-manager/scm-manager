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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.PublicKey;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultGPGTest {

  @Mock
  private PublicKeyStore store;

  @InjectMocks
  private DefaultGPG gpg;

  @Test
  void shouldFindIdInSignature() throws IOException {
    String raw = GPGTestHelper.readKey("single.asc");
    String publicKeyId = gpg.findPublicKeyId(raw.getBytes());

    assertThat(publicKeyId).isEqualTo("0x975922F193B07D6E");
  }

  @Test
  void shouldFindPublicKey() {
    RawGpgKey key1 = new RawGpgKey("42", "key_42", "trillian", "raw", Instant.now());

    when(store.findById("42")).thenReturn(Optional.of(key1));

    Optional<PublicKey> publicKey = gpg.findPublicKey("42");

    assertThat(publicKey).isPresent();
    assertThat(publicKey.get().getOwner()).isPresent();
    assertThat(publicKey.get().getOwner().get()).contains("trillian");
    assertThat(publicKey.get().getId()).isEqualTo("42");
  }

  @Test
  void shouldFindKeysForUsername() {
    RawGpgKey key1 = new RawGpgKey("1", "1", "trillian", "raw", Instant.now());
    RawGpgKey key2 = new RawGpgKey("2", "2", "trillian", "raw", Instant.now());
    when(store.findByUsername("trillian")).thenReturn(ImmutableList.of(key1, key2));

    Iterable<PublicKey> keys = gpg.findPublicKeysByUsername("trillian");

    assertThat(keys).hasSize(2);
    PublicKey key = keys.iterator().next();
    assertThat(key.getOwner()).isPresent();
    assertThat(key.getOwner().get()).contains("trillian");
  }
}
