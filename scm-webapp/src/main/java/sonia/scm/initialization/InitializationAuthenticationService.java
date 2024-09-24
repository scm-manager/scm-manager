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
