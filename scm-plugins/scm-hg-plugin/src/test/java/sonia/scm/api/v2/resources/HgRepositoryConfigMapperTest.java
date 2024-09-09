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
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgRepositoryConfig;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgRepositoryConfigMapperTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HgConfigLinks links;

  @InjectMocks
  private HgRepositoryConfigMapperImpl mapper;

  @Mock
  private Subject subject;

  @BeforeEach
  void setUpSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapToDto() {
    Repository repository = RepositoryTestData.createHeartOfGold("hg");
    when(links.repository(repository).get()).thenReturn("/hg/config");

    HgRepositoryConfig config = new HgRepositoryConfig();
    config.setEncoding("UTF-8");

    HgRepositoryConfigDto dto = mapper.map(repository, config);
    assertThat(dto.getEncoding()).isEqualTo("UTF-8");
    assertThat(dto.getLinks().getLinkBy("self")).hasValueSatisfying(
      link -> assertThat(link.getHref()).isEqualTo("/hg/config")
    );
  }

  @Test
  void shouldAppendUpdateLink() {
    Repository repository = RepositoryTestData.createHeartOfGold("hg");
    when(links.repository(repository).get()).thenReturn("/hg/config");
    when(links.repository(repository).update()).thenReturn("/hg/config/update");
    when(subject.isPermitted("repository:hg:" + repository.getId())).thenReturn(true);

    HgRepositoryConfigDto dto = mapper.map(repository, new HgRepositoryConfig());
    assertThat(dto.getLinks().getLinkBy("update")).hasValueSatisfying(
      link -> assertThat(link.getHref()).isEqualTo("/hg/config/update")
    );
  }

}
