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

import com.google.common.collect.ImmutableSet;
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
import sonia.scm.config.ScmConfiguration;
import sonia.scm.notifications.NotificationSender;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.FullHealthCheckCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static com.google.common.collect.ImmutableSet.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
  private ScmConfiguration scmConfiguration;
  @Mock
  private NotificationSender notificationSender;

  @Mock
  private Subject subject;

  private HealthChecker checker;

  @BeforeEach
  void initializeChecker() {
    this.checker = new HealthChecker(of(healthCheck1, healthCheck2), repositoryManager, repositoryServiceFactory, postProcessor, scmConfiguration, notificationSender);
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
      when(subject.getPrincipal()).thenReturn("trillian");
      when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.unhealthy(createFailure("error1")));
      when(healthCheck2.check(repository)).thenReturn(HealthCheckResult.unhealthy(createFailure("error2")));

      checker.lightCheck(repositoryId);

      verify(postProcessor).setCheckResults(eq(repository), argThat(failures -> {
        assertThat(failures)
          .hasSize(2)
          .extracting("id").containsExactly("error1", "error2");
        return true;
      }));
    }

    @Test
    void shouldLockWhileLightCheckIsRunning() throws InterruptedException {
      CountDownLatch waitUntilSecondCheckHasRun = new CountDownLatch(1);
      CountDownLatch waitForFirstCheckStarted = new CountDownLatch(1);

      when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.healthy());
      when(healthCheck2.check(repository)).thenAnswer(invocation -> {
        waitForFirstCheckStarted.countDown();
        waitUntilSecondCheckHasRun.await();
        return HealthCheckResult.healthy();
      });

      new Thread(() -> checker.lightCheck(repositoryId)).start();

      waitForFirstCheckStarted.await();
      await().until(() -> {
        checker.lightCheck(repositoryId);
        return true;
      });

      waitUntilSecondCheckHasRun.countDown();

      verify(healthCheck1).check(repository);
    }

    @Test
    void shouldShowRunningCheck() throws InterruptedException {
      CountDownLatch waitUntilVerification = new CountDownLatch(1);
      CountDownLatch waitForFirstCheckStarted = new CountDownLatch(1);

      assertThat(checker.checkRunning(repositoryId)).isFalse();

      when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.healthy());
      when(healthCheck2.check(repository)).thenAnswer(invocation -> {
        waitForFirstCheckStarted.countDown();
        waitUntilVerification.await();
        return HealthCheckResult.healthy();
      });

      new Thread(() -> checker.lightCheck(repositoryId)).start();

      waitForFirstCheckStarted.await();

      assertThat(checker.checkRunning(repositoryId)).isTrue();

      waitUntilVerification.countDown();

      await().until(() -> !checker.checkRunning(repositoryId));
    }

    @Nested
    class ForFullChecks {

      @Mock
      private FullHealthCheckCommandBuilder fullHealthCheckCommand;

      @BeforeEach
      void setUpRepository() {
        lenient().when(repositoryServiceFactory.create(repository)).thenReturn(repositoryService);
        lenient().when(repositoryService.getFullCheckCommand()).thenReturn(fullHealthCheckCommand);
        lenient().when(subject.getPrincipal()).thenReturn("trillian");
      }

      @Test
      void shouldComputeLightChecksForFullChecks() {
        when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.healthy());
        when(healthCheck2.check(repository)).thenReturn(HealthCheckResult.unhealthy(createFailure("error")));

        checker.fullCheck(repositoryId);

        verify(postProcessor).setCheckResults(eq(repository), argThat(failures -> {
          assertThat(failures)
            .hasSize(1)
            .extracting("id").containsExactly("error");
          return true;
        }));
      }

      @Test
      void shouldLockWhileFullCheckIsRunning() throws InterruptedException {
        CountDownLatch waitUntilSecondCheckHasRun = new CountDownLatch(1);
        CountDownLatch waitForFirstCheckStarted = new CountDownLatch(1);

        when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.healthy());
        when(healthCheck2.check(repository)).thenAnswer(invocation -> {
          waitForFirstCheckStarted.countDown();
          waitUntilSecondCheckHasRun.await();
          return HealthCheckResult.healthy();
        });

        new Thread(() -> checker.fullCheck(repositoryId)).start();

        waitForFirstCheckStarted.await();
        await().until(() -> {
          checker.fullCheck(repositoryId);
          return true;
        });

        waitUntilSecondCheckHasRun.countDown();

        verify(healthCheck1).check(repository);
      }

      @Test
      void shouldComputeFullChecks() throws IOException {
        when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.healthy());
        when(healthCheck2.check(repository)).thenReturn(HealthCheckResult.healthy());
        when(repositoryService.isSupported(Command.FULL_HEALTH_CHECK)).thenReturn(true);
        when(fullHealthCheckCommand.check()).thenReturn(HealthCheckResult.unhealthy(createFailure("error")));

        checker.fullCheck(repositoryId);

        verify(postProcessor).setCheckResults(eq(repository), argThat(failures -> {
          assertThat(failures)
            .hasSize(1)
            .extracting("id").containsExactly("error");
          return true;
        }));
      }

      @Test
      void shouldNotifyCurrentUserOnlyOnce() throws IOException {
        when(scmConfiguration.getEmergencyContacts()).thenReturn(ImmutableSet.of("trillian"));
        when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.healthy());
        when(repositoryService.isSupported(Command.FULL_HEALTH_CHECK)).thenReturn(true);
        when(fullHealthCheckCommand.check()).thenReturn(HealthCheckResult.unhealthy(createFailure("error")));

        checker.fullCheck(repositoryId);

        verify(notificationSender, times(1)).send(any());
        verify(notificationSender, never()).send(any(), eq("trillian"));
      }

      @Test
      void shouldNotifyEmergencyContacts() throws IOException {
        when(scmConfiguration.getEmergencyContacts()).thenReturn(ImmutableSet.of("trillian", "Arthur"));
        when(healthCheck1.check(repository)).thenReturn(HealthCheckResult.healthy());
        when(repositoryService.isSupported(Command.FULL_HEALTH_CHECK)).thenReturn(true);
        when(fullHealthCheckCommand.check()).thenReturn(HealthCheckResult.unhealthy(createFailure("error")));

        checker.fullCheck(repositoryId);

        verify(notificationSender).send(any());
        verify(notificationSender).send(any(), eq("Arthur"));
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
