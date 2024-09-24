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

package sonia.scm.repository;

import com.github.sdorra.ssp.PermissionActionCheckInterceptor;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.function.BooleanSupplier;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPermissionGuardTest {

  @Mock
  private Subject subject;
  @Mock
  private BooleanSupplier permittedDelegate;
  @Mock
  private Runnable checkDelegate;

  @BeforeAll
  static void setReadOnlyVerbs() {
    RepositoryPermissionGuard.setReadOnlyVerbs(singletonList("read"));
  }

  @Nested
  class ForReadOnlyVerb {

    PermissionActionCheckInterceptor<Repository> readInterceptor = new RepositoryPermissionGuard().intercept("read");

    @Test
    void shouldNotInterceptPermissionCheck() {
      when(permittedDelegate.getAsBoolean()).thenReturn(true);

      assertThat(readInterceptor.isPermitted(subject, "1", permittedDelegate)).isTrue();

      verify(permittedDelegate).getAsBoolean();
    }

    @Test
    void shouldNotInterceptCheckRequest() {
      readInterceptor.check(subject, "1", checkDelegate);

      verify(checkDelegate).run();
    }
  }

  @Nested
  class ForModifyingVerb {

    PermissionActionCheckInterceptor<Repository> readInterceptor = new RepositoryPermissionGuard().intercept("modify");

    @Nested
    class WithNormalRepository {

      @Test
      void shouldInterceptPermissionCheck() {
        when(permittedDelegate.getAsBoolean()).thenReturn(true);

        assertThat(readInterceptor.isPermitted(subject, "1", permittedDelegate)).isTrue();

        verify(permittedDelegate).getAsBoolean();
      }

      @Test
      void shouldInterceptCheckRequest() {
        readInterceptor.check(subject, "1", checkDelegate);

        verify(checkDelegate).run();
      }
    }

    @Nested
    class WithArchivedRepository {

      @BeforeEach
      void mockArchivedRepository() {
        RepositoryPermissionGuard.setReadOnlyChecks(Collections.singleton(new EventDrivenRepositoryArchiveCheck()));
        EventDrivenRepositoryArchiveCheck.setAsArchived("1");
      }

      @AfterEach
      void removeArchiveFlag() {
        EventDrivenRepositoryArchiveCheck.removeFromArchived("1");
        RepositoryPermissionGuard.setReadOnlyChecks(Collections.emptySet());
      }

      @Test
      void shouldInterceptPermissionCheck() {
        assertThat(readInterceptor.isPermitted(subject, "1", permittedDelegate)).isFalse();

        verify(permittedDelegate, never()).getAsBoolean();
      }

      @Test
      void shouldInterceptCheckRequest() {
        assertThrows(AuthorizationException.class, () -> readInterceptor.check(subject, "1", checkDelegate));
      }

      @Test
      void shouldThrowConcretePermissionExceptionOverArchiveException() {
        doThrow(new AuthorizationException()).when(checkDelegate).run();

        assertThrows(AuthorizationException.class, () -> readInterceptor.check(subject, "1", checkDelegate));

        verify(checkDelegate).run();
      }
    }

    @Nested
    @ExtendWith(WrapInExportCheck.class)
    class WithExportingRepository {

      @Test
      void shouldInterceptPermissionCheck() {
        assertThat(readInterceptor.isPermitted(subject, "1", permittedDelegate)).isFalse();

        verify(permittedDelegate, never()).getAsBoolean();
      }

      @Test
      void shouldInterceptCheckRequest() {
        assertThrows(AuthorizationException.class, () -> readInterceptor.check(subject, "1", checkDelegate));
      }

      @Test
      void shouldThrowConcretePermissionExceptionOverArchiveException() {
        doThrow(new AuthorizationException()).when(checkDelegate).run();

        assertThrows(AuthorizationException.class, () -> readInterceptor.check(subject, "1", checkDelegate));

        verify(checkDelegate).run();
      }
    }
  }

  private static class WrapInExportCheck implements InvocationInterceptor, AfterEachCallback {

    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) {
      DefaultRepositoryExportingCheck check = new DefaultRepositoryExportingCheck();
      RepositoryPermissionGuard.setReadOnlyChecks(Collections.singleton(check));
      check.withExportingLock(new Repository("1", "git", "space", "X"), () -> {
        try {
          invocation.proceed();
          return null;
        } catch (Throwable t) {
          throw new RuntimeException(t);
        }
      });
    }

    @Override
    public void afterEach(ExtensionContext context) {
      RepositoryPermissionGuard.setReadOnlyChecks(Collections.emptySet());
    }
  }
}
