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

import org.apache.shiro.authc.AuthenticationException;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.KeyGenerator;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.web.security.AdministrationContext;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

@Singleton
public class InitializationAuthenticationService {

  private String initToken;
  private final Provider<InitializationFinisher> initializationFinisherProvider;
  private final PermissionAssigner permissionAssigner;
  private final AccessTokenCookieIssuer cookieIssuer;
  private final InitializationCookieIssuer initializationCookieIssuer;

  private final AdministrationContext administrationContext;

  @Inject
  public InitializationAuthenticationService(KeyGenerator generator, Provider<InitializationFinisher> initializationFinisherProvider, PermissionAssigner permissionAssigner, AccessTokenCookieIssuer cookieIssuer, InitializationCookieIssuer initializationCookieIssuer, AdministrationContext administrationContext) {
    this.initToken = generator.createKey();
    this.initializationFinisherProvider = initializationFinisherProvider;
    this.permissionAssigner = permissionAssigner;
    this.cookieIssuer = cookieIssuer;
    this.initializationCookieIssuer = initializationCookieIssuer;
    this.administrationContext = administrationContext;
  }

  public void validateToken(String token) {
    if (initializationFinisherProvider.get().isFullyInitialized()) {
      invalidateToken();
    }
    if (token == null || !token.equals(initToken)) {
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
    initializationCookieIssuer.authenticateForInitialization(request, response, initToken);
  }

  public void invalidateCookies(HttpServletRequest request, HttpServletResponse response) {
    cookieIssuer.invalidate(request, response);
  }

  private void invalidateToken() {
    this.initToken = null;
  }
}
