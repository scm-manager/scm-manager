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

package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.api.v2.resources.HgGlobalConfigTestUtil.assertEqualsConfiguration;
import static sonia.scm.api.v2.resources.HgGlobalConfigTestUtil.createConfiguration;

@ExtendWith(MockitoExtension.class)
class HgGlobalConfigToHgGlobalConfigDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  private final URI expectedBaseUri = baseUri.resolve(HgConfigResource.HG_CONFIG_PATH_V2);

  @Mock
  private Subject subject;

  @Mock
  private RepositoryManager manager;

  private HgGlobalConfigToHgGlobalConfigDtoMapper mapper;

  @BeforeEach
  void init() {
    ThreadContext.bind(subject);

    ScmPathInfoStore store = new ScmPathInfoStore();
    store.set(() -> baseUri);

    mapper = Mappers.getMapper(HgGlobalConfigToHgGlobalConfigDtoMapper.class);
    mapper.setLinks(new HgConfigLinks(store));
    mapper.setRepositoryManager(manager);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapFields() {
    HgGlobalConfig config = createConfiguration();

    when(subject.isPermitted("configuration:write:hg")).thenReturn(true);
    when(manager.getAll()).thenReturn(Collections.singleton(RepositoryTestData.create42Puzzle("hg")));
    HgGlobalGlobalConfigDto dto = mapper.map(config);

    assertEqualsConfiguration(dto);
    assertThat(dto.isAllowDisable()).isFalse();

    assertThat(dto.getLinks().getLinkBy("self")).hasValueSatisfying(
      link -> assertThat(link.getHref()).isEqualTo(expectedBaseUri.toString())
    );
    assertThat(dto.getLinks().getLinkBy("update")).hasValueSatisfying(
      link -> assertThat(link.getHref()).isEqualTo(expectedBaseUri.toString())
    );
    assertThat(dto.getLinks().getLinkBy("autoConfiguration")).hasValueSatisfying(
      link -> assertThat(link.getHref()).isEqualTo(expectedBaseUri + "/auto-configuration")
    );
  }

  @Test
  void shouldAllowDisableIfNoHgRepositoriesExist() {
    HgGlobalConfig config = createConfiguration();

    when(subject.isPermitted("configuration:write:hg")).thenReturn(true);
    when(manager.getAll()).thenReturn(Collections.singleton(RepositoryTestData.create42Puzzle("git")));
    HgGlobalGlobalConfigDto dto = mapper.map(config);

    assertThat(dto.isAllowDisable()).isTrue();
  }

  @Test
  void shouldMapFieldsWithoutUpdate() {
    HgGlobalConfig config = createConfiguration();

    when(subject.isPermitted("configuration:write:hg")).thenReturn(false);
    HgGlobalGlobalConfigDto dto = mapper.map(config);

    assertThat(dto.getLinks().getLinkBy("self")).hasValueSatisfying(
      link -> assertThat(link.getHref()).isEqualTo(expectedBaseUri.toString())
    );
    assertThat(dto.getLinks().hasLink("update")).isFalse();
    assertThat(dto.getLinks().hasLink("autoConfiguration")).isFalse();
  }
}
