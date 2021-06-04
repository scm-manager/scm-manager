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

package sonia.scm.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.GPG;
import sonia.scm.security.PublicKey;

import java.util.Optional;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GPGSignatureResolverTest {

  @Mock
  private GPG gpg;

  @Test
  void shouldDelegateFindPublicKeyId() {
    GPGSignatureResolver signatureResolver = new GPGSignatureResolver(gpg, emptySet());

    byte[] signature = new byte[]{4, 2};
    when(gpg.findPublicKeyId(signature)).thenReturn("0x42");

    assertThat(signatureResolver.findPublicKeyId(signature)).isEqualTo("0x42");
  }

  @Test
  void shouldResolveStoredGpgKey() {
    GPGSignatureResolver signatureResolver = new GPGSignatureResolver(gpg, emptySet());

    PublicKey publicKey = createPublicKey("0x42");
    when(gpg.findPublicKey("0x42")).thenReturn(Optional.of(publicKey));

    Optional<PublicKey> resolverPublicKey = signatureResolver.findPublicKey("0x42");
    assertThat(resolverPublicKey).contains(publicKey);
  }

  @Test
  void shouldResolveKeyFormList() {
    PublicKey publicKey = createPublicKey("0x21");
    GPGSignatureResolver signatureResolver = new GPGSignatureResolver(gpg, singleton(publicKey));

    Optional<PublicKey> resolverPublicKey = signatureResolver.findPublicKey("0x21");
    assertThat(resolverPublicKey).contains(publicKey);
  }

  @Test
  void shouldResolveSubkeyFormList() {
    PublicKey publicKey = createPublicKey("0x21", "0x42");
    GPGSignatureResolver signatureResolver = new GPGSignatureResolver(gpg, singleton(publicKey));

    Optional<PublicKey> resolverPublicKey = signatureResolver.findPublicKey("0x42");
    assertThat(resolverPublicKey).contains(publicKey);
  }

  private PublicKey createPublicKey(String id, String... subkeys) {
    PublicKey publicKey = mock(PublicKey.class);
    lenient().when(publicKey.getId()).thenReturn(id);
    lenient().when(publicKey.getSubkeys()).thenReturn(ImmutableSet.copyOf(subkeys));
    return publicKey;
  }

}
