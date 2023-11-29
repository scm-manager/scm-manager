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

package sonia.scm.initialization;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.web.security.AdministrationContext;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Singleton
public class InitializationAuthenticationService {

  private static final String INITIALIZATION_SUBJECT = "SCM-INIT";

  private final AccessTokenBuilderFactory tokenBuilderFactory;
  private final PermissionAssigner permissionAssigner;
  private final AccessTokenCookieIssuer cookieIssuer;
  private final InitializationCookieIssuer initializationCookieIssuer;

  private final AdministrationContext administrationContext;

  @Inject
  public InitializationAuthenticationService(AccessTokenBuilderFactory tokenBuilderFactory, PermissionAssigner permissionAssigner, AccessTokenCookieIssuer cookieIssuer, InitializationCookieIssuer initializationCookieIssuer, AdministrationContext administrationContext) {
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.permissionAssigner = permissionAssigner;
    this.cookieIssuer = cookieIssuer;
    this.initializationCookieIssuer = initializationCookieIssuer;
    this.administrationContext = administrationContext;
  }

  public void validateToken(AccessToken token) {
    if (token == null || !INITIALIZATION_SUBJECT.equals(token.getSubject())) {
      throw new AuthenticationException("Could not authenticate to initialization realm because of missing or invalid token.");
    }
  }

  public void setPermissions() {
    administrationContext.runAsAdmin(() -> permissionAssigner.setPermissionsForUser(
      InitializationRealm.INIT_PRINCIPAL,
      Set.of(new PermissionDescriptor("plugin:read,write"))
    ));
  }

  public void authenticate(HttpServletRequest request, HttpServletResponse response) {
    AccessToken initToken =
      tokenBuilderFactory.create()
        .subject(INITIALIZATION_SUBJECT)
        .expiresIn(365, TimeUnit.DAYS)
        .refreshableFor(0, TimeUnit.SECONDS)
        .build();
    initializationCookieIssuer.authenticateForInitialization(request, response, initToken);
  }

  public void invalidateCookies(HttpServletRequest request, HttpServletResponse response) {
    cookieIssuer.invalidate(request, response);
  }
}
