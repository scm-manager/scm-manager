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

import com.google.common.io.Resources;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.ImportRepositoryHookEvent;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnbundleCommandBuilder;
import sonia.scm.repository.api.UnbundleResponse;
import sonia.scm.repository.work.WorkdirProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Consumer;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("UnstableApiUsage")
class FromBundleImporterTest {

  public static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold("svn");
  @Mock
  private RepositoryManager manager;
  @Mock
  private RepositoryHandler repositoryHandler;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private ScmEventBus eventBus;
  @Mock
  private WorkdirProvider workdirProvider;
  @Mock
  private RepositoryImportLoggerFactory loggerFactory;
  @Mock
  private RepositoryImportLogger logger;
  @Mock(answer = Answers.RETURNS_SELF)
  private UnbundleCommandBuilder unbundleCommandBuilder;
  @Mock
  private Subject subject;

  @InjectMocks
  private FromBundleImporter importer;

  @BeforeEach
  void mockSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void cleanupSubject() {
    ThreadContext.unbindSubject();
  }

  @Nested
  class WithPermission {

    @BeforeEach
    void initMocks(@TempDir Path temp) throws IOException {
      when(subject.getPrincipal()).thenReturn("dent");
      when(workdirProvider.createNewWorkdir(REPOSITORY.getId())).thenReturn(temp.toFile());
      when(manager.create(eq(REPOSITORY), any())).thenAnswer(
        invocation -> {
          invocation.getArgument(1, Consumer.class).accept(REPOSITORY);
          return REPOSITORY;
        }
      );
      when(manager.getHandler("svn")).thenReturn(repositoryHandler);
      RepositoryType repositoryType = mock(RepositoryType.class);
      when(repositoryHandler.getType()).thenReturn(repositoryType);
      when(repositoryType.getSupportedCommands()).thenReturn(singleton(Command.UNBUNDLE));
      when(loggerFactory.createLogger()).thenReturn(logger);

      when(unbundleCommandBuilder.unbundle(any(File.class))).thenReturn(new UnbundleResponse(42));
      RepositoryService service = mock(RepositoryService.class);
      when(serviceFactory.create(any(Repository.class))).thenReturn(service);
      when(service.getUnbundleCommand()).thenReturn(unbundleCommandBuilder);
    }

    @Test
    void shouldImportCompressedBundle() throws IOException {
      InputStream in = buildInput("sonia/scm/api/v2/svn.dump.gz");

      importer.importFromBundle(true, in, REPOSITORY);

      verify(unbundleCommandBuilder).setCompressed(true);
      verify(unbundleCommandBuilder).unbundle(any(File.class));
    }

    @Test
    void shouldImportNonCompressedBundle() throws IOException {
      InputStream in = buildInput("sonia/scm/api/v2/svn.dump");

      importer.importFromBundle(false, in, REPOSITORY);

      verify(unbundleCommandBuilder, never()).setCompressed(true);
      verify(unbundleCommandBuilder).unbundle(any(File.class));
    }

    @Test
    void shouldSetPermissionForCurrentUser() throws IOException {
      InputStream in = buildInput("sonia/scm/api/v2/svn.dump");

      Repository createdRepository = importer.importFromBundle(false, in, REPOSITORY);

      assertThat(createdRepository.getPermissions())
        .hasSize(1);
      RepositoryPermission permission = createdRepository.getPermissions().iterator().next();
      assertThat(permission.getName()).isEqualTo("dent");
      assertThat(permission.isGroupPermission()).isFalse();
      assertThat(permission.getRole()).isEqualTo("OWNER");
    }

    @Test
    void shouldPostEvents() throws IOException {
      InputStream in = buildInput("sonia/scm/api/v2/svn.dump");

      RepositoryHookEvent event = mock(RepositoryHookEvent.class);
      when(unbundleCommandBuilder.setPostEventSink(any()))
        .thenAnswer(invocationOnMock -> {
          invocationOnMock.getArgument(0, Consumer.class).accept(event);
          return invocationOnMock.getMock();
        });

      importer.importFromBundle(false, in, REPOSITORY);

      verify(eventBus).post(argThat(o -> o instanceof PostReceiveRepositoryHookEvent));
      verify(eventBus).post(argThat(o -> o instanceof ImportRepositoryHookEvent));
    }
  }

  @Test
  void shouldFailWithoutPermission() throws IOException {
    InputStream in = buildInput("sonia/scm/api/v2/svn.dump");

    doThrow(new AuthorizationException()).when(subject).checkPermission("repository:create");

    assertThrows(AuthorizationException.class, () -> importer.importFromBundle(false, in, REPOSITORY));

    verify(manager, never()).create(any(), any());
  }

  private InputStream buildInput(String s) throws IOException {
    URL dumpUrl = Resources.getResource(s);
    return new ByteArrayInputStream(Resources.toByteArray(dumpUrl));
  }
}
