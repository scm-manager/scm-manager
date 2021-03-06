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

package sonia.scm.importexport;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryImportEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.function.Consumer;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.api.Command.PULL;

@ExtendWith(MockitoExtension.class)
class FromUrlImporterTest {

  @Mock
  private RepositoryManager manager;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private ScmEventBus eventBus;
  @Mock
  private RepositoryImportLoggerFactory loggerFactory;
  @Mock
  private RepositoryImportLogger logger;
  @Mock
  private Subject subject;

  @InjectMocks
  private FromUrlImporter importer;

  private final Repository repository = RepositoryTestData.createHeartOfGold("git");

  @BeforeEach
  void setUpMocks() {
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(loggerFactory.createLogger()).thenReturn(logger);
    when(manager.create(any(), any())).thenAnswer(
      invocation -> {
        Repository repository = invocation.getArgument(0, Repository.class);
        Repository createdRepository = repository.clone();
        createdRepository.setNamespace("created");
        invocation.getArgument(1, Consumer.class).accept(createdRepository);
        return createdRepository;
      }
    );
  }

  @BeforeEach
  void setUpRepositoryType() {
    RepositoryHandler repositoryHandler = mock(RepositoryHandler.class);
    when(manager.getHandler(repository.getType())).thenReturn(repositoryHandler);
    RepositoryType repositoryType = mock(RepositoryType.class);
    when(repositoryHandler.getType()).thenReturn(repositoryType);
    when(repositoryType.getSupportedCommands()).thenReturn(singleton(PULL));
  }

  @BeforeEach
  void mockSubject() {
    ThreadContext.bind(subject);
    when(subject.getPrincipal()).thenReturn("trillian");
  }

  @AfterEach
  void cleanupSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldPullChangesFromRemoteUrl() throws IOException {
    PullCommandBuilder pullCommandBuilder = mock(PullCommandBuilder.class, RETURNS_SELF);
    when(service.getPullCommand()).thenReturn(pullCommandBuilder);

    FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
    parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");

    Repository createdRepository = importer.importFromUrl(parameters, repository);

    assertThat(createdRepository.getNamespace()).isEqualTo("created");
    verify(pullCommandBuilder).pull("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
    verify(logger).finished();
    verify(eventBus).post(argThat(
      event -> {
        assertThat(event).isInstanceOf(RepositoryImportEvent.class);
        RepositoryImportEvent repositoryImportEvent = (RepositoryImportEvent) event;
        assertThat(repositoryImportEvent.getItem().getNamespace()).isEqualTo("created");
        assertThat(repositoryImportEvent.isFailed()).isFalse();
        return true;
      }
    ));
  }

  @Test
  void shouldPullChangesFromRemoteUrlWithCredentials() {
    PullCommandBuilder pullCommandBuilder = mock(PullCommandBuilder.class, RETURNS_SELF);
    when(service.getPullCommand()).thenReturn(pullCommandBuilder);

    FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
    parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
    parameters.setUsername("trillian");
    parameters.setPassword("secret");

    importer.importFromUrl(parameters, repository);

    verify(pullCommandBuilder).withUsername("trillian");
    verify(pullCommandBuilder).withPassword("secret");
  }

  @Test
  void shouldThrowImportFailedEvent() throws IOException {
    PullCommandBuilder pullCommandBuilder = mock(PullCommandBuilder.class, RETURNS_SELF);
    when(service.getPullCommand()).thenReturn(pullCommandBuilder);
    doThrow(TestException.class).when(pullCommandBuilder).pull(anyString());
    when(logger.started()).thenReturn(true);

    FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
    parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");

    assertThrows(ImportFailedException.class, () -> importer.importFromUrl(parameters, repository));
    verify(logger).failed(argThat(e -> e instanceof TestException));
    verify(eventBus).post(argThat(
      event -> {
        assertThat(event).isInstanceOf(RepositoryImportEvent.class);
        RepositoryImportEvent repositoryImportEvent = (RepositoryImportEvent) event;
        assertThat(repositoryImportEvent.getItem()).isEqualTo(repository);
        assertThat(repositoryImportEvent.isFailed()).isTrue();
        return true;
      }
    ));
  }

  private static class TestException extends RuntimeException {}
}
