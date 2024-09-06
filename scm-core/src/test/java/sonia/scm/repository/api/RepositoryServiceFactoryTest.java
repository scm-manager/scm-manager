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
import sonia.scm.repository.DefaultRepositoryExportingCheck;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.spi.RepositoryServiceProvider;
import sonia.scm.repository.spi.RepositoryServiceResolver;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.user.EMail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceFactoryTest {

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
      cacheManager, repositoryManager, builder.build(),
      preProcessorUtil, ImmutableSet.of(), workdirProvider,
      new EMail(new ScmConfiguration()), eventBus,
      new DefaultRepositoryExportingCheck()
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
