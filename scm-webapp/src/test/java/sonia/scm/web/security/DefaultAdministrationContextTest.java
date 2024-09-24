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
import sonia.scm.security.Impersonator;

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

    context = new DefaultAdministrationContext(injector, new Impersonator(securityManager));
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
