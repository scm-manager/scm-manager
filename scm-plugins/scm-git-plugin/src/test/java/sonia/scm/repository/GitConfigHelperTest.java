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

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class GitConfigHelperTest {
  @Mock
  Repository gitRepository;

  StoredConfig gitConfig;

  sonia.scm.repository.Repository scmmRepository;

  @BeforeEach
  void setUp() {
    gitConfig = new StoredConfig() {
      @Override
      public void load() {
        // not needed
      }

      @Override
      public void save() {
        // not needed
      }
    };
    doReturn(gitConfig).when(gitRepository).getConfig();

    scmmRepository = RepositoryTestData.createHeartOfGold();
  }

  @Test
  void shouldSetCorrectScmmRepositoryId() throws IOException {
    GitConfigHelper target = new GitConfigHelper();

    target.createScmmConfig(scmmRepository, gitRepository);

    assertThat(gitConfig.getString("scmm", null, "repositoryid")).isEqualTo(scmmRepository.getId());
  }

  @Test
  void shouldAllowUploadpackFilter() throws IOException {
    GitConfigHelper target = new GitConfigHelper();

    target.createScmmConfig(scmmRepository, gitRepository);

    assertThat(gitConfig.getBoolean("uploadpack",  "allowFilter", false)).isTrue();
  }
}
