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
    assertThat(dto.getCreated()).isEqualTo(key.getCreated());
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/v2/users/trillian/public_keys/1");
    assertThat(dto.getLinks().getLinkBy("delete").get().getHref()).isEqualTo("/v2/users/trillian/public_keys/1");
    assertThat(dto.getLinks().getLinkBy("raw").get().getHref()).isEqualTo("/v2/public_keys/1");
  }

  @Test
  void shouldNotAppendDeleteLinkIfPermissionMissing() throws IOException {
    String raw = GPGTestHelper.readResourceAsString("single.asc");
    RawGpgKey key = new RawGpgKey("1", "key_42", "trillian", raw, Collections.emptySet(), Instant.now());

    RawGpgKeyDto dto = mapper.map(key);

    assertThat(dto.getLinks().getLinkBy("delete")).isNotPresent();
  }

  @Test
  void shouldNotAppendDeleteLinkIfReadonly() throws IOException {
    when(subject.isPermitted("user:changePublicKeys:trillian")).thenReturn(true);

    String raw = GPGTestHelper.readResourceAsString("single.asc");
    RawGpgKey key = new RawGpgKey("1", "key_42", "trillian", raw, Collections.emptySet(), Instant.now(), true);

    RawGpgKeyDto dto = mapper.map(key);

    assertThat(dto.getLinks().getLinkBy("delete")).isNotPresent();
  }
}
