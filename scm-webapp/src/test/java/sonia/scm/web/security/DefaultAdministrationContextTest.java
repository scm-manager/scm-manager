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

package sonia.scm.web.security;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.Authentications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAdministrationContextTest {

  private DefaultAdministrationContext context;

  @Mock
  private Subject subject;

  @BeforeEach
  void create() {
    Injector injector = Guice.createInjector();
    SecurityManager securityManager = new DefaultSecurityManager();

    context = new DefaultAdministrationContext(injector, securityManager);
  }

  @Test
  void shouldBindSubject() {
    context.runAsAdmin(() -> {
      Subject adminSubject = SecurityUtils.getSubject();
      assertThat(adminSubject.getPrincipal()).isEqualTo(Authentications.PRINCIPAL_SYSTEM);
    });
  }

  @Test
  void shouldBindSubjectEvenIfAlreadyBound() {
    ThreadContext.bind(subject);
    try {

      context.runAsAdmin(() -> {
        Subject adminSubject = SecurityUtils.getSubject();
        assertThat(adminSubject.getPrincipal()).isEqualTo(Authentications.PRINCIPAL_SYSTEM);
      });

    } finally {
      ThreadContext.unbindSubject();
    }
  }

  @Test
  void shouldRestoreCurrentSubject() {
    when(subject.getPrincipal()).thenReturn("tricia");
    ThreadContext.bind(subject);
    try {
      context.runAsAdmin(() -> {});
      Subject currentSubject = SecurityUtils.getSubject();
      assertThat(currentSubject.getPrincipal()).isEqualTo("tricia");
    } finally {
      ThreadContext.unbindSubject();
    }
  }

}
