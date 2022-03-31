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

package sonia.scm.repository.cli;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryToRepositoryCommandDtoMapperTest {

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;

  private RepositoryToRepositoryCommandDtoMapperImpl mapper;

  @BeforeEach
  void initMapper() {
    mapper = new RepositoryToRepositoryCommandDtoMapperImpl();
    mapper.setServiceFactory(serviceFactory);
  }

  @Test
  void shouldMapAttributes() {
    Repository testRepo = RepositoryTestData.create42Puzzle();
    when(serviceFactory.create(testRepo)).thenReturn(service);
    RepositoryCommandDto dto = mapper.map(testRepo);

    assertThat(dto.getNamespace()).isEqualTo(testRepo.getNamespace());
    assertThat(dto.getName()).isEqualTo(testRepo.getName());
    assertThat(dto.getContact()).isEqualTo(testRepo.getContact());
    assertThat(dto.getDescription()).isEqualTo(testRepo.getDescription());
  }

  @Test
  void shouldAppendHttpUrl() {
    ScmProtocol scmProtocol = new ScmProtocol() {
      @Override
      public String getType() {
        return "http";
      }

      @Override
      public String getUrl() {
        return "http://localhost:8081/scm";
      }
    };
    Repository testRepo = RepositoryTestData.create42Puzzle();

    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(testRepo)).thenReturn(service);
    when(service.getSupportedProtocols()).thenReturn(ImmutableList.of(scmProtocol).stream());

    RepositoryCommandDto dto = mapper.map(testRepo);

    assertThat(dto.getUrl()).isEqualTo("http://localhost:8081/scm");
  }
}
