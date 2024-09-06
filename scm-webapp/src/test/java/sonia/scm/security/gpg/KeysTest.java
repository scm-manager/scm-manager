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

import com.google.common.collect.ImmutableList;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.ScmConstraintViolationException;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.security.gpg.GPGTestHelper.readResourceAsString;

@ExtendWith(MockitoExtension.class)
class KeysTest {

  @Test
  void shouldResolveSingleId() throws IOException {
    String rawPublicKey = readResourceAsString("single.asc");
    Keys keys = Keys.resolve(rawPublicKey);
    assertThat(keys.getMaster()).isEqualTo("0x975922F193B07D6E");
  }

  @Test
  void shouldResolveIdsFromSubkeys() throws IOException {
    String rawPublicKey = readResourceAsString("subkeys.asc");
    Keys keys = Keys.resolve(rawPublicKey);
    assertThat(keys.getMaster()).isEqualTo("0x13B13D4C8A9350A1");
    assertThat(keys.getSubs()).containsOnly("0x247E908C6FD35473", "0xE50E1DD8B90D3A6B", "0xBF49759E43DD0E60");
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForMultipleMasterKeys() {
    PGPPublicKey one = mockMasterKey(42L);
    PGPPublicKey two = mockMasterKey(21L);

    assertThrows(ScmConstraintViolationException.class, () -> Keys.resolve("", raw -> ImmutableList.of(one, two)));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWithoutMasterKey() {
    assertThrows(ScmConstraintViolationException.class, () -> Keys.resolve("", raw -> Collections.emptyList()));
  }

  private PGPPublicKey mockMasterKey(long id) {
    PGPPublicKey key = mock(PGPPublicKey.class);
    when(key.isMasterKey()).thenReturn(true);
    lenient().when(key.getKeyID()).thenReturn(id);
    return key;
  }

}
