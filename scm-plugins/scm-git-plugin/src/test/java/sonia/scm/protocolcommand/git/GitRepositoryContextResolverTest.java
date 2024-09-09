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

package sonia.scm.protocolcommand.git;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryManager;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitRepositoryContextResolverTest {

  private static final Repository REPOSITORY = new Repository("id", "git", "space", "X");

  @Mock
  RepositoryManager repositoryManager;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  RepositoryLocationResolver locationResolver;
  @Mock(answer = Answers.CALLS_REAL_METHODS)
  ScmConfiguration scmConfiguration;

  @InjectMocks
  GitRepositoryContextResolver resolver;

  @BeforeEach
  void mockConfiguration() {
    when(scmConfiguration.getBaseUrl()).thenReturn("https://hog.hitchhiker.com/scm");
  }

  @Nested
  class WithRepository {

    @TempDir
    Path repositoryPath;

    @BeforeEach
    void mockRepository() {
      when(scmConfiguration.getBaseUrl()).thenReturn("https://hog.hitchhiker.com/scm");
      when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(REPOSITORY);
      when(locationResolver.forClass(any()).getLocation("id")).thenReturn(repositoryPath);
    }

    @Test
    void shouldResolveCorrectRepository() {
      RepositoryContext context = resolver.resolve(new String[]{"git", "repo/space/X/something/else"});

      assertThat(context.getRepository()).isSameAs(REPOSITORY);
      assertThat(context.getDirectory()).isEqualTo(repositoryPath.resolve("data"));
    }

    @Test
    void shouldResolveCorrectRepositoryWithContextPath() {
      RepositoryContext context = resolver.resolve(new String[]{"git", "scm/repo/space/X/something/else"});

      assertThat(context.getRepository()).isSameAs(REPOSITORY);
      assertThat(context.getDirectory()).isEqualTo(repositoryPath.resolve("data"));
    }
  }

  @Test
  void shouldRejectWrongContextPath() {
    assertThrows(NotFoundException.class,
      () -> resolver.resolve(new String[]{"git", "vogons/repo/space/X/something/else"}));
  }
}
