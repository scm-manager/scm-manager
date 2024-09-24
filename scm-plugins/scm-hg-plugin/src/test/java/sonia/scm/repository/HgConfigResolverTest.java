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

package sonia.scm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgConfigResolverTest {

  @Mock
  private HgRepositoryHandler handler;

  @Mock
  private HgRepositoryConfigStore repositoryConfigStore;

  private HgConfigResolver resolver;

  private Repository heartOfGold = RepositoryTestData.createHeartOfGold("hg");
  private HgGlobalConfig globalConfig;
  private HgRepositoryConfig repositoryConfig;

  @BeforeEach
  void setUpResolver(@TempDir Path directory) {
    globalConfig = new HgGlobalConfig();
    repositoryConfig = new HgRepositoryConfig();

    when(handler.getDirectory(heartOfGold.getId())).thenReturn(directory.toFile());
    when(handler.getConfig()).thenReturn(globalConfig);

    when(repositoryConfigStore.of(heartOfGold)).thenReturn(repositoryConfig);

    resolver = new HgConfigResolver(handler, repositoryConfigStore);
  }

  @Test
  void shouldReturnEncodingFromRepositoryConfig() {
    globalConfig.setEncoding("ISO-8859-1");
    repositoryConfig.setEncoding("ISO-8859-15");

    HgConfig config = resolver.resolve(heartOfGold);
    assertThat(config.getEncoding()).isEqualTo("ISO-8859-15");
  }

  @Test
  void shouldReturnEncodingFromGlobalConfig() {
    globalConfig.setEncoding("ISO-8859-1");

    HgConfig config = resolver.resolve(heartOfGold);
    assertThat(config.getEncoding()).isEqualTo("ISO-8859-1");
  }
}
