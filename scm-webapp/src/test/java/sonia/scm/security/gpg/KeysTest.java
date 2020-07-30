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
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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

    assertThrows(IllegalArgumentException.class, () -> Keys.resolve("", raw -> ImmutableList.of(one, two)));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWithoutMasterKey() {
    assertThrows(IllegalArgumentException.class, () -> Keys.resolve("", raw -> Collections.emptyList()));
  }

  private PGPPublicKey mockMasterKey(long id) {
    PGPPublicKey key = mock(PGPPublicKey.class);
    when(key.isMasterKey()).thenReturn(true);
    lenient().when(key.getKeyID()).thenReturn(id);
    return key;
  }

}
