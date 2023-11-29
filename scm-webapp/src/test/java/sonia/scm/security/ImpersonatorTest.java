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
