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

import de.otto.edison.hal.HalRepresentation;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class UserPublicKeyResourceTest {

  @Mock
  private PublicKeyStore store;

  @Mock
  private PublicKeyCollectionMapper collectionMapper;

  @Mock
  private PublicKeyMapper mapper;

  @InjectMocks
  private UserPublicKeyResource resource;

  @Mock
  private Subject subject;

  @BeforeEach
  void setUpSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void clearSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldFindAll() {
    List<RawGpgKey> keys = new ArrayList<>();
    when(store.findByUsername("trillian")).thenReturn(keys);

    HalRepresentation collection = new HalRepresentation();
    when(collectionMapper.map("trillian", keys)).thenReturn(collection);

    HalRepresentation result = resource.findAll("trillian");
    assertThat(result).isSameAs(collection);
  }

  @Test
  void shouldFindByIdJson() {
    RawGpgKey key = new RawGpgKey("42");
    when(store.findById("42")).thenReturn(Optional.of(key));
    RawGpgKeyDto dto = new RawGpgKeyDto();
    when(mapper.map(key)).thenReturn(dto);

    Response response = resource.findByIdJson("42");
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isSameAs(dto);
  }

  @Test
  void shouldReturn404IfIdDoesNotExists() {
    when(store.findById("42")).thenReturn(Optional.empty());

    Response response = resource.findByIdJson("42");
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  void shouldAddToStore() throws URISyntaxException, IOException {
    String raw = GPGTestHelper.readResourceAsString("single.asc");

    UriInfo uriInfo = mock(UriInfo.class);
    UriBuilder builder = mock(UriBuilder.class);
    when(uriInfo.getAbsolutePathBuilder()).thenReturn(builder);
    when(builder.path("42")).thenReturn(builder);
    when(builder.build()).thenReturn(new URI("/v2/public_keys/42"));

    RawGpgKey key = new RawGpgKey("42");
    RawGpgKeyDto dto = new RawGpgKeyDto();
    dto.setDisplayName("key_42");
    dto.setRaw(raw);
    when(store.add(dto.getDisplayName(), "trillian", dto.getRaw())).thenReturn(key);

    Response response = resource.create(uriInfo, "trillian", dto);

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getLocation().toASCIIString()).isEqualTo("/v2/public_keys/42");
  }

  @Test
  void shouldDeleteFromStore() {
    Response response = resource.deleteById("42");
    assertThat(response.getStatus()).isEqualTo(204);
    verify(store).delete("42");
  }

}
