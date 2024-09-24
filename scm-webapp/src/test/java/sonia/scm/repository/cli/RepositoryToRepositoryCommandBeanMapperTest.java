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

package sonia.scm.repository.cli;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class RepositoryToRepositoryCommandBeanMapperTest {

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
    RepositoryCommandBean dto = mapper.map(testRepo);

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

    RepositoryCommandBean dto = mapper.map(testRepo);

    assertThat(dto.getUrl()).isEqualTo("http://localhost:8081/scm");
  }
}
