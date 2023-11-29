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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.DefaultRepositoryExportingCheck;
import sonia.scm.repository.EventDrivenRepositoryArchiveCheck;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryArchivedException;
import sonia.scm.repository.RepositoryExportingException;
import sonia.scm.repository.RepositoryReadOnlyCheckerHack;
import sonia.scm.repository.spi.HttpScmProtocol;
import sonia.scm.repository.spi.RepositoryServiceProvider;
import sonia.scm.user.EMail;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.util.IterableUtil.sizeOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

  private final RepositoryServiceProvider provider = mock(RepositoryServiceProvider.class);
  private final Repository repository = new Repository("42", "git", "space", "repo");

  private final EMail eMail = new EMail(new ScmConfiguration());

  @Mock
  private Subject subject;

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldReturnMatchingProtocolsFromProvider() {
    when(subject.getPrincipal()).thenReturn("Hitchhiker");
    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null, Collections.singleton(new DummyScmProtocolProvider()), null, eMail, null);
    Stream<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols();

    assertThat(sizeOf(supportedProtocols.collect(Collectors.toList()))).isEqualTo(1);
  }

  @Test
  void shouldFilterOutNonAnonymousEnabledProtocolsForAnonymousUser() {
    when(subject.getPrincipal()).thenReturn(SCMContext.USER_ANONYMOUS);
    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null, Stream.of(new DummyScmProtocolProvider(), new DummyScmProtocolProvider(false)).collect(Collectors.toSet()), null, eMail, null);
    Stream<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols();

    assertThat(sizeOf(supportedProtocols.collect(Collectors.toList()))).isEqualTo(1);
  }

  @Test
  void shouldFindKnownProtocol() {
    when(subject.getPrincipal()).thenReturn("Hitchhiker");
    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null, Collections.singleton(new DummyScmProtocolProvider()), null, eMail, null);

    HttpScmProtocol protocol = repositoryService.getProtocol(HttpScmProtocol.class);

    assertThat(protocol).isNotNull();
  }

  @Test
  void shouldFailForUnknownProtocol() {
    when(subject.getPrincipal()).thenReturn("Hitchhiker");
    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null, Collections.singleton(new DummyScmProtocolProvider()), null, eMail, null);

    assertThrows(IllegalArgumentException.class, () -> repositoryService.getProtocol(UnknownScmProtocol.class));
  }

  @Nested
  class RepositoryReadOnlyCheckerTests {

    @AfterEach
    void clearChecks() {
      RepositoryReadOnlyCheckerHack.setReadOnlyChecks(Collections.emptySet());
      repository.setArchived(false);
    }

    @Test
    void shouldFailForExportingRepository() {
      DefaultRepositoryExportingCheck check = new DefaultRepositoryExportingCheck();

      RepositoryReadOnlyCheckerHack.setReadOnlyChecks(Collections.singleton(check));

      check.withExportingLock(repository, () -> {
        RepositoryService repositoryService = new RepositoryService(null, provider, repository, null, Collections.singleton(new DummyScmProtocolProvider()), null, eMail, null);

        assertThrows(RepositoryExportingException.class, repositoryService::getModifyCommand);
        assertThrows(RepositoryExportingException.class, repositoryService::getBranchCommand);
        assertThrows(RepositoryExportingException.class, repositoryService::getPullCommand);
        assertThrows(RepositoryExportingException.class, repositoryService::getTagCommand);
        assertThrows(RepositoryExportingException.class, repositoryService::getMergeCommand);
        assertThrows(RepositoryExportingException.class, repositoryService::getModifyCommand);
        return null;
      });
    }

    @Test
    void shouldFailForArchivedRepository() {
      EventDrivenRepositoryArchiveCheck check = new EventDrivenRepositoryArchiveCheck();
      RepositoryReadOnlyCheckerHack.setReadOnlyChecks(Collections.singleton(check));

      repository.setArchived(true);

      RepositoryService repositoryService = new RepositoryService(null, provider, repository, null, Collections.singleton(new DummyScmProtocolProvider()), null, eMail, null);

      assertThrows(RepositoryArchivedException.class, repositoryService::getModifyCommand);
      assertThrows(RepositoryArchivedException.class, repositoryService::getBranchCommand);
      assertThrows(RepositoryArchivedException.class, repositoryService::getPullCommand);
      assertThrows(RepositoryArchivedException.class, repositoryService::getTagCommand);
      assertThrows(RepositoryArchivedException.class, repositoryService::getMergeCommand);
      assertThrows(RepositoryArchivedException.class, repositoryService::getModifyCommand);
    }

  }


  private static class DummyHttpProtocol extends HttpScmProtocol {

    private final boolean anonymousEnabled;

    public DummyHttpProtocol(Repository repository, boolean anonymousEnabled) {
      super(repository, "");
      this.anonymousEnabled = anonymousEnabled;
    }

    @Override
    public void serve(HttpServletRequest request, HttpServletResponse response, Repository repository, ServletConfig config) {
    }

    @Override
    public boolean isAnonymousEnabled() {
      return anonymousEnabled;
    }
  }

  private static class DummyScmProtocolProvider implements ScmProtocolProvider<ScmProtocol> {

    private final boolean anonymousEnabled;

    public DummyScmProtocolProvider() {
      this(true);
    }

    public DummyScmProtocolProvider(boolean anonymousEnabled) {
      this.anonymousEnabled = anonymousEnabled;
    }

    @Override
    public String getType() {
      return "git";
    }

    @Override
    public ScmProtocol get(Repository repository) {
      return new DummyHttpProtocol(repository, anonymousEnabled);
    }
  }

  private interface UnknownScmProtocol extends ScmProtocol {
  }
}
