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

package sonia.scm.importexport;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
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
import sonia.scm.repository.api.PullResponse;
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
import static org.mockito.ArgumentMatchers.eq;
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
  @Mock(answer = Answers.RETURNS_SELF)
  private PullCommandBuilder pullCommandBuilder;
  @Mock
  private ImportNotificationHandler notificationHandler;

  @InjectMocks
  private FromUrlImporter importer;

  private final Repository repository = RepositoryTestData.createHeartOfGold("git");
  private Repository createdRepository;

  private PullResponse mockedResponse = new PullResponse();

  @BeforeEach
  void setUpMocks() {
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(loggerFactory.createLogger()).thenReturn(logger);
    when(manager.create(any(), any())).thenAnswer(
      invocation -> {
        Repository repository = invocation.getArgument(0, Repository.class);
        createdRepository = repository.clone();
        createdRepository.setNamespace("created");
        invocation.getArgument(1, Consumer.class).accept(createdRepository);
        return createdRepository;
      }
    );
    when(service.getPullCommand()).thenReturn(pullCommandBuilder);
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

  @Nested
  class ForSuccessfulImports {

    @BeforeEach
    void mockImportResult() throws IOException {
      when(pullCommandBuilder.pull(anyString())).thenAnswer(invocation -> mockedResponse);
    }

    @Test
    void shouldPullChangesFromRemoteUrl() throws IOException {
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
      verify(notificationHandler)
        .handleSuccessfulImport(
          eq(createdRepository),
          argThat(argument -> argument.getSuccessCount() == 0));
    }

    @Test
    void shouldPullChangesFromRemoteUrlWithCredentials() {
      FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
      parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
      parameters.setUsername("trillian");
      parameters.setPassword("secret");

      importer.importFromUrl(parameters, repository);

      verify(pullCommandBuilder).withUsername("trillian");
      verify(pullCommandBuilder).withPassword("secret");
    }

    @Test
    void shouldPullChangesWithLfsIfNotDisabled() {
      FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
      parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
      parameters.setSkipLfs(false);

      importer.importFromUrl(parameters, repository);

      verify(pullCommandBuilder).doFetchLfs(true);
    }

    @Test
    void shouldPullChangesWithoutLfsIfSelected() {
      FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
      parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
      parameters.setSkipLfs(true);

      importer.importFromUrl(parameters, repository);

      verify(pullCommandBuilder).doFetchLfs(false);
    }

    @Test
    void shouldHandleFailedLfsFiles() {
      FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
      parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
      parameters.setSkipLfs(false);
      mockedResponse = new PullResponse(42, new PullResponse.LfsCount(0, 1));

      importer.importFromUrl(parameters, repository);

      verify(notificationHandler).
        handleSuccessfulImportWithLfsFailures(
          eq(createdRepository),
          argThat(argument -> argument.getFailureCount() == 1 && argument.getSuccessCount() == 0));
    }
  }

  @Test
  void shouldThrowImportFailedEvent() throws IOException {
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
