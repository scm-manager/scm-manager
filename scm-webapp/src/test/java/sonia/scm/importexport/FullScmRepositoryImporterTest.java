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

import com.google.common.io.Resources;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.ImportRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryImportEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.IncompatibleEnvironmentForImportException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnbundleCommandBuilder;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.update.UpdateEngine;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@SuppressWarnings("UnstableApiUsage")
class FullScmRepositoryImporterTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold("svn");

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService service;
  @Mock
  private UnbundleCommandBuilder unbundleCommandBuilder;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private ScmEnvironmentCompatibilityChecker compatibilityChecker;
  @Mock
  private TarArchiveRepositoryStoreImporter storeImporter;
  @Mock
  private UpdateEngine updateEngine;
  @Mock
  private RepositoryImportExportEncryption repositoryImportExportEncryption;
  @Mock
  private WorkdirProvider workdirProvider;
  @Mock
  private RepositoryImportLogger logger;
  @Mock
  private RepositoryImportLoggerFactory loggerFactory;
  @Mock
  private Subject subject;
  @Mock
  private ScmEventBus eventBus;
  @Mock
  private AdministrationContext administrationContext;

  @InjectMocks
  private EnvironmentCheckStep environmentCheckStep;
  @InjectMocks
  private MetadataImportStep metadataImportStep;
  @InjectMocks
  private StoreImportStep storeImportStep;
  @InjectMocks
  private RepositoryImportStep repositoryImportStep;

  @Mock
  private RepositoryHookEvent event;

  @Captor
  private ArgumentCaptor<Consumer<RepositoryHookEvent>> eventSinkCaptor;

  private FullScmRepositoryImporter fullImporter;

  @BeforeEach
  void initTestObject() {
    fullImporter = new FullScmRepositoryImporter(
      environmentCheckStep,
      metadataImportStep,
      storeImportStep,
      repositoryImportStep,
      repositoryManager,
      repositoryImportExportEncryption,
      loggerFactory,
      eventBus);
  }

  @BeforeEach
  void mockAdminContext() {
    lenient().doAnswer(invocation -> {
      invocation.getArgument(0, PrivilegedAction.class).run();
      return null;
    }).when(administrationContext).runAsAdmin(any(PrivilegedAction.class));
  }

  @BeforeEach
  void initRepositoryService() {
    lenient().when(serviceFactory.create(REPOSITORY)).thenReturn(service);
    lenient().when(service.getUnbundleCommand()).thenReturn(unbundleCommandBuilder);
    lenient().when(loggerFactory.createLogger()).thenReturn(logger);
  }

  @BeforeEach
  void initSubject() {
    ThreadContext.bind(subject);
    lenient().when(subject.getPrincipal()).thenReturn("Trillian");
  }

  @BeforeEach
  void cleanupSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldNotImportRepositoryIfFileNotExists(@TempDir Path temp) throws IOException {
    Path emptyFile = temp.resolve("empty");
    Files.createFile(emptyFile);
    try (FileInputStream inputStream = new FileInputStream(emptyFile.toFile())) {
      assertThrows(
        ImportFailedException.class,
        () -> fullImporter.importFromStream(REPOSITORY, inputStream, "")
      );
    }
  }

  @Test
  void shouldFailIfScmEnvironmentIsIncompatible() throws IOException {
    when(compatibilityChecker.check(any())).thenReturn(false);
    InputStream importStream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();
    assertThrows(
      IncompatibleEnvironmentForImportException.class,
      () -> fullImporter.importFromStream(REPOSITORY, importStream, "")
    );

    verify(eventBus).post(argThat(
      event -> {
        assertThat(event).isInstanceOf(RepositoryImportEvent.class);
        RepositoryImportEvent repositoryImportEvent = (RepositoryImportEvent) event;
        assertThat(repositoryImportEvent.getItem()).isEqualTo(REPOSITORY);
        assertThat(repositoryImportEvent.isFailed()).isTrue();
        return true;
      }
    ));
  }

  @Test
  void shouldNotImportRepositoryWithoutPermission() throws IOException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("repository:create");

    InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();

    assertThrows(AuthorizationException.class, () -> fullImporter.importFromStream(REPOSITORY, stream, null));

    verify(storeImporter, never()).importFromTarArchive(any(Repository.class), any(InputStream.class), any(RepositoryImportLogger.class));
    verify(repositoryManager, never()).modify(any());
  }

  @Nested
  class WithValidEnvironment {

    @BeforeEach
    void setUpEnvironment(@TempDir Path temp) {
      lenient().when(workdirProvider.createNewWorkdir(REPOSITORY.getId())).thenReturn(temp.toFile());

      when(compatibilityChecker.check(any())).thenReturn(true);
      when(repositoryManager.create(REPOSITORY)).thenReturn(REPOSITORY);
    }

    @Test
    void shouldImportScmRepositoryArchiveWithWorkDir() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();

      Repository repository = fullImporter.importFromStream(REPOSITORY, stream, "");

      assertThat(repository).isEqualTo(REPOSITORY);
      verify(storeImporter).importFromTarArchive(eq(REPOSITORY), any(InputStream.class), eq(logger));
      verify(repositoryManager).modify(REPOSITORY);
      Collection<RepositoryPermission> updatedPermissions = REPOSITORY.getPermissions();
      assertThat(updatedPermissions).hasSize(3);
      verify(unbundleCommandBuilder).unbundle((InputStream) argThat(argument -> argument.getClass().equals(NoneClosingInputStream.class)));
      verify(workdirProvider, times(1)).createNewWorkdir(REPOSITORY.getId());
    }

    @Test
    void shouldNotExistWorkDirAfterRepositoryImportIsFinished(@TempDir Path temp) throws IOException {
      when(workdirProvider.createNewWorkdir(REPOSITORY.getId())).thenReturn(temp.toFile());
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();
      fullImporter.importFromStream(REPOSITORY, stream, "");

      boolean workDirExists = Files.exists(temp);
      assertThat(workDirExists).isFalse();
    }

    @Test
    void shouldSendImportedEventForImportedRepository() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();
      when(unbundleCommandBuilder.setPostEventSink(any())).thenAnswer(
        invocation -> {
          invocation.getArgument(0, Consumer.class).accept(new RepositoryHookEvent(null, REPOSITORY, null));
          return null;
        }
      );
      ArgumentCaptor<Object> capturedEvents = ArgumentCaptor.forClass(Object.class);
      doNothing().when(eventBus).post(capturedEvents.capture());

      fullImporter.importFromStream(REPOSITORY, stream, null);

      assertThat(capturedEvents.getAllValues()).hasSize(2);
      assertThat(capturedEvents.getAllValues()).anyMatch(
        event ->
          event instanceof ImportRepositoryHookEvent &&
            ((ImportRepositoryHookEvent) event).getRepository().equals(REPOSITORY)
      );
      assertThat(capturedEvents.getAllValues()).anyMatch(
        event ->
          event instanceof RepositoryImportEvent &&
            ((RepositoryImportEvent) event).getItem().equals(REPOSITORY)
      );
    }

    @Test
    void shouldTriggerUpdateForImportedRepository() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();
      fullImporter.importFromStream(REPOSITORY, stream, "");

      verify(updateEngine).update(REPOSITORY.getId());
    }

    @Test
    void shouldImportRepositoryDirectlyWithoutCopyInWorkDir() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import-stores-before-repository.tar.gz").openStream();
      Repository repository = fullImporter.importFromStream(REPOSITORY, stream, "");

      assertThat(repository).isEqualTo(REPOSITORY);
      verify(storeImporter).importFromTarArchive(eq(REPOSITORY), any(InputStream.class), eq(logger));
      verify(repositoryManager).create(REPOSITORY);
      verify(repositoryManager).modify(REPOSITORY);
      verify(unbundleCommandBuilder).unbundle((InputStream) argThat(argument -> argument.getClass().equals(NoneClosingInputStream.class)));
      verify(workdirProvider, never()).createNewWorkdir(REPOSITORY.getId());
    }

    @Test
    void shouldFireImportRepositoryHookEvent() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();

      // capture the event sink which is registered by RepositoryImportStep
      // we use when here instead of verify, because for verify we have used it before
      when(unbundleCommandBuilder.setPostEventSink(eventSinkCaptor.capture())).thenReturn(unbundleCommandBuilder);
      // when the RepositoryImportStep calls unbundle we pass our mocked event to the captured sink
      when(unbundleCommandBuilder.unbundle(any(InputStream.class))).then(ic -> {
        eventSinkCaptor.getValue().accept(event);
        // RepositoryImportStep does not evaluate the return value of unbundle
        return null;
      });

      fullImporter.importFromStream(REPOSITORY, stream, "");

      verify(eventBus).post(any(ImportRepositoryHookEvent.class));
    }
  }
}
