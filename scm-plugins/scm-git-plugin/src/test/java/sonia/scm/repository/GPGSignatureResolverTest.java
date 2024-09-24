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
