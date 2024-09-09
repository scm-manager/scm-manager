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

package sonia.scm.repository.spi;

import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryTestData;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GitRepositoryServiceResolverTest {

  @Mock
  private Injector injector;

  @Mock
  private GitContextFactory contextFactory;

  @InjectMocks
  private GitRepositoryServiceResolver resolver;

  @Test
  void shouldCreateRepositoryServiceProvider() {
    GitRepositoryServiceProvider provider = resolver.resolve(RepositoryTestData.createHeartOfGold("git"));
    assertThat(provider).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = { "hg","svn", "unknown"})
  void shouldReturnNullForNonGitRepositories(String type) {
    GitRepositoryServiceProvider provider = resolver.resolve(RepositoryTestData.createHeartOfGold(type));
    assertThat(provider).isNull();
  }
}
