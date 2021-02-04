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

import com.github.sdorra.ssp.PermissionActionCheckInterceptor;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
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
        EventDrivenRepositoryArchiveCheck.setAsArchived("1");
      }

      @AfterEach
      void removeArchiveFlag() {
        EventDrivenRepositoryArchiveCheck.removeFromArchived("1");
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

  private static class WrapInExportCheck implements InvocationInterceptor {

    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) {
      new DefaultRepositoryExportingCheck().withExportingLock(new Repository("1", "git", "space", "X"), () -> {
        try {
          invocation.proceed();
          return null;
        } catch (Throwable t) {
          throw new RuntimeException(t);
        }
      });
    }
  }
}
