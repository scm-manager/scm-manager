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

package sonia.scm.repository;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.FullHealthCheckCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;

import static com.google.common.collect.ImmutableSet.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckerTest {

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  private final String repositoryId = repository.getId();

  @Mock
  private HealthCheck healthCheck1;
  @Mock
  private HealthCheck healthCheck2;

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryServiceFactory repositoryServiceFactory;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private RepositoryPostProcessor postProcessor;

  @Mock
  private Subject subject;

  private HealthChecker checker;

  @BeforeEach
  void initializeChecker() {
    this.checker = new HealthChecker(of(healthCheck1, healthCheck2), repositoryManager, repositoryServiceFactory, postProcessor);
  }

  @BeforeEach
  void initSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void cleanupSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldFailForNotExistingRepositoryId() {
    assertThrows(NotFoundException.class, () -> checker.lightCheck("no-such-id"));
  }

  @Nested
  class WithRepository {
    @BeforeEach
    void setUpRepository() {
      doReturn(repository).when(repositoryManager).get(repositoryId);
    }

    @Test
    void shouldComputeLightChecks() {
      when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.unhealthy(createFailure("error1")));
      when(healthCheck2.check(repository)).thenReturn(HealthCheckResult.unhealthy(createFailure("error2")));

      checker.lightCheck(repositoryId);

      verify(postProcessor).setCheckResults(eq(repositoryId), argThat(failures -> {
        assertThat(failures)
          .hasSize(2)
          .extracting("id").containsExactly("error1", "error2");
        return true;
      }));
    }

    @Nested
    class ForFullChecks {

      @Mock
      private FullHealthCheckCommandBuilder fullHealthCheckCommand;

      @BeforeEach
      void setUpRepository() {
        when(repositoryServiceFactory.create(repository)).thenReturn(repositoryService);
        lenient().when(repositoryService.getFullCheckCommand()).thenReturn(fullHealthCheckCommand);
      }

      @Test
      void shouldComputeLightChecksForFullChecks() {
        when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.healthy());
        when(healthCheck2.check(repository)).thenReturn(HealthCheckResult.unhealthy(createFailure("error")));

        checker.fullCheck(repositoryId);

        verify(postProcessor).setCheckResults(eq(repositoryId), argThat(failures -> {
          assertThat(failures)
            .hasSize(1)
            .extracting("id").containsExactly("error");
          return true;
        }));
      }

      @Test
      void shouldComputeFullChecks() throws IOException {
        when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.healthy());
        when(healthCheck2.check(repository)).thenReturn(HealthCheckResult.healthy());
        when(repositoryService.isSupported(Command.FULL_HEALTH_CHECK)).thenReturn(true);
        when(fullHealthCheckCommand.check()).thenReturn(HealthCheckResult.unhealthy(createFailure("error")));

        checker.fullCheck(repositoryId);

        verify(postProcessor).setCheckResults(eq(repositoryId), argThat(failures -> {
          assertThat(failures)
            .hasSize(1)
            .extracting("id").containsExactly("error");
          return true;
        }));
      }
    }
  }

  @Nested
  class WithoutPermission {

    @BeforeEach
    void setMissingPermission() {
      doThrow(AuthorizationException.class).when(subject).checkPermission("repository:healthCheck:" + repositoryId);
    }

    @Test
    void shouldFailToRunLightChecksWithoutPermissionForId() {
      assertThrows(AuthorizationException.class, () -> checker.lightCheck(repositoryId));
    }

    @Test
    void shouldFailToRunLightChecksWithoutPermissionForRepository() {
      assertThrows(AuthorizationException.class, () -> checker.lightCheck(repository));
    }

    @Test
    void shouldFailToRunFullChecksWithoutPermissionForId() {
      assertThrows(AuthorizationException.class, () -> checker.fullCheck(repositoryId));
    }

    @Test
    void shouldFailToRunFullChecksWithoutPermissionForRepository() {
      assertThrows(AuthorizationException.class, () -> checker.fullCheck(repository));
    }
  }

  private HealthCheckFailure createFailure(String text) {
    return new HealthCheckFailure(text, text, text);
  }
}
