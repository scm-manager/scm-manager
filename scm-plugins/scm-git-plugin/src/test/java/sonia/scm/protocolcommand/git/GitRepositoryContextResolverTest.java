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
