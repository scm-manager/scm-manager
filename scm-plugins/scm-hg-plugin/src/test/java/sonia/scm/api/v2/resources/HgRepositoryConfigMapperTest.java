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
 *
 */

package sonia.scm.api.v2.resources;


import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
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
