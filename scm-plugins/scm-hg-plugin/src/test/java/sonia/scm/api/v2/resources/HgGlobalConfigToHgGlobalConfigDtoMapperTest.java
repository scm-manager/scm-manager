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
