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

package sonia.scm.security;


import jakarta.annotation.Nonnull;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.security.Impersonator.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImpersonatorTest {

  private SecurityManager securityManager = new DefaultSecurityManager();

  private Impersonator impersonator;

  @BeforeEach
  void setUp() {
    impersonator = new Impersonator(securityManager);
  }

  @Test
  void shouldBindAndRestoreNonWebThread() {
    try (Session session = impersonator.impersonate(principal("dent"))) {
      assertPrincipal("dent");
    }
    assertThrows(UnavailableSecurityManagerException.class, SecurityUtils::getSubject);
  }

  @Nonnull
  private SimplePrincipalCollection principal(String principal) {
    return new SimplePrincipalCollection(principal, "test");
  }

  private void assertPrincipal(String principal) {
    assertThat(SecurityUtils.getSubject().getPrincipal()).isEqualTo(principal);
  }

  @Nested
  @ExtendWith(ShiroExtension.class)
  class WithSecurityManager {

    @Test
    @SubjectAware("trillian")
    void shouldBindAndRestoreWebThread() {
      assertPrincipal("trillian");

      try (Session session = impersonator.impersonate(principal("slarti"))) {
        assertPrincipal("slarti");
      }

      assertPrincipal("trillian");
    }

  }
}
