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

import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import de.otto.edison.hal.HalRepresentation;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicKeyCollectionMapperTest {


  private PublicKeyCollectionMapper collectionMapper;

  @Mock
  private PublicKeyMapper mapper;

  @Mock
  private Subject subject;

  @BeforeEach
  void setUpObjectUnderTest() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));
    collectionMapper = new PublicKeyCollectionMapper(Providers.of(pathInfoStore), mapper);

    ThreadContext.bind(subject);
  }

  @AfterEach
  void cleanThreadContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapToCollection() throws IOException {
    when(mapper.map(any(RawGpgKey.class))).then(ic -> new RawGpgKeyDto());

    RawGpgKey one = createPublicKey("one");
    RawGpgKey two = createPublicKey("two");

    List<RawGpgKey> keys = Lists.newArrayList(one, two);
    HalRepresentation collection = collectionMapper.map("trillian", keys);

    List<HalRepresentation> embedded = collection.getEmbedded().getItemsBy("keys");
    assertThat(embedded).hasSize(2);

    assertThat(collection.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/v2/users/trillian/public_keys");
  }

  @Test
  void shouldAddCreateLinkIfTheUserIsPermitted() {
    when(subject.isPermitted("user:changePublicKeys:trillian")).thenReturn(true);

    HalRepresentation collection = collectionMapper.map("trillian", Lists.newArrayList());
    assertThat(collection.getLinks().getLinkBy("create").get().getHref()).isEqualTo("/v2/users/trillian/public_keys");
  }

  @Test
  void shouldNotAddCreateLinkWithoutPermission() {
    HalRepresentation collection = collectionMapper.map("trillian", Lists.newArrayList());
    assertThat(collection.getLinks().getLinkBy("create")).isNotPresent();
  }

  private RawGpgKey createPublicKey(String displayName) throws IOException {
    String raw = GPGTestHelper.readResourceAsString("single.asc");
    return new RawGpgKey(displayName, displayName, "trillian", raw, Collections.emptySet(), Instant.now());
  }

}
