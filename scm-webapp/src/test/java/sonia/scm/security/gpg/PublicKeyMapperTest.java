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

import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicKeyMapperTest {

  @Mock
  private Subject subject;

  private final PublicKeyMapper mapper = new PublicKeyMapperImpl();
  ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();

  @BeforeEach
  void setup() {
    ThreadContext.bind(subject);

    pathInfoStore.set(() -> URI.create("/"));
    mapper.setScmPathInfoStore(Providers.of(pathInfoStore));
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapKeyToDto() throws IOException {
    when(subject.isPermitted("user:changePublicKeys:trillian")).thenReturn(true);

    String raw = GPGTestHelper.readResourceAsString("single.asc");
    RawGpgKey key = new RawGpgKey("1", "key_42", "trillian", raw, Collections.emptySet(), Instant.now());

    RawGpgKeyDto dto = mapper.map(key);

    assertThat(dto.getDisplayName()).isEqualTo(key.getDisplayName());
    assertThat(dto.getRaw()).isEqualTo(key.getRaw());
    assertThat(dto.getCreated()).isEqualTo(key.getCreated());
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/v2/public_keys/1");
    assertThat(dto.getLinks().getLinkBy("delete").get().getHref()).isEqualTo("/v2/public_keys/delete/1");
  }

  @Test
  void shouldNotAppendDeleteLink() throws IOException {
    String raw = GPGTestHelper.readResourceAsString("single.asc");
    RawGpgKey key = new RawGpgKey("1", "key_42", "trillian", raw, Collections.emptySet(), Instant.now());

    RawGpgKeyDto dto = mapper.map(key);

    assertThat(dto.getLinks().getLinkBy("delete")).isNotPresent();
  }
}
