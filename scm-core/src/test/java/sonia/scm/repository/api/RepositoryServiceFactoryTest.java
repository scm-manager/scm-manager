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

package sonia.scm.repository.api;

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.spi.RepositoryServiceProvider;
import sonia.scm.repository.spi.RepositoryServiceResolver;
import sonia.scm.repository.util.WorkdirProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceFactoryTest {

  @Mock
  private ScmConfiguration configuration;

  @Mock(answer = Answers.RETURNS_MOCKS)
  private CacheManager cacheManager;

  @Mock
  private RepositoryManager repositoryManager;

  @Mock
  private WorkdirProvider workdirProvider;

  @Mock
  private PreProcessorUtil preProcessorUtil;

  @Mock
  private ScmEventBus eventBus;

  private RepositoryServiceFactory factory;

  @Mock
  private Subject subject;

  private final Repository repository = new Repository(
    "AmRy34KFi1", "git", "hitchhiker", "heart-of-gold"
  );

  @BeforeEach
  void setUpFactory() {
    factory = createFactory(null);
  }

  private RepositoryServiceFactory createFactory(RepositoryServiceResolver repositoryServiceResolver) {
    ImmutableSet.Builder<RepositoryServiceResolver> builder = ImmutableSet.builder();
    if (repositoryServiceResolver != null) {
      builder.add(repositoryServiceResolver);
    }
    return new RepositoryServiceFactory(
      configuration, cacheManager, repositoryManager, builder.build(),
      preProcessorUtil, ImmutableSet.of(), workdirProvider, eventBus
    );
  }

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldThrowAuthorizationException() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("repository:read:AmRy34KFi1");
    assertThrows(AuthorizationException.class, () -> factory.create(repository));
  }

  @Test
  void shouldThrowNotFoundExceptionForId() {
    assertThrows(NotFoundException.class, () -> factory.create("AmRy34KFi1"));
  }

  @Test
  void shouldThrowNotFoundExceptionForNamespaceAndName() {
    NamespaceAndName namespaceAndName = new NamespaceAndName("hitchhiker", "heart-of-gold");
    assertThrows(NotFoundException.class, () -> factory.create(namespaceAndName));
  }

  @Test
  void shouldThrowRepositoryServiceNotFoundException() {
    when(repositoryManager.get("AmRy34KFi1")).thenReturn(repository);
    assertThrows(RepositoryServiceNotFoundException.class, () -> factory.create("AmRy34KFi1"));
  }

  @Test
  void shouldCreateRepositoryService() {
    RepositoryServiceResolver repositoryServiceResolver = mock(RepositoryServiceResolver.class);
    factory = createFactory(repositoryServiceResolver);
    RepositoryServiceProvider repositoryServiceProvider = mock(RepositoryServiceProvider.class);
    when(repositoryServiceResolver.resolve(repository)).thenReturn(repositoryServiceProvider);

    RepositoryService repositoryService = factory.create(repository);
    assertThat(repositoryService).isNotNull();
  }

}
