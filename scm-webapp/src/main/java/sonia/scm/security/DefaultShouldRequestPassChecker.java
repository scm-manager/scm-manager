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

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.api.v2.resources.HealthCheckResource;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgentParser;

public class DefaultShouldRequestPassChecker implements ShouldRequestPassChecker {

  private final ScmConfiguration configuration;
  private final UserAgentParser userAgentParser;

  @Inject
  public DefaultShouldRequestPassChecker(ScmConfiguration configuration, UserAgentParser userAgentParser) {
    this.configuration = configuration;
    this.userAgentParser = userAgentParser;
  }

  @Override
  public boolean shouldPass(HttpServletRequest request) {
    return isUserAuthenticated()
      || isAnonymousProtocolRequest(request)
      || isMercurialHookRequest(request)
      || (!isLoginRequest(request) && isFullAnonymousAccessEnabled())
      || isHealthCheckRequest(request);
  }

  private boolean isUserAuthenticated() {
    Subject subject = SecurityUtils.getSubject();
    return subject.isAuthenticated() && !Authentications.isAuthenticatedSubjectAnonymous();
  }

  private boolean isAnonymousProtocolRequest(HttpServletRequest request) {
    return !HttpUtil.isWUIRequest(request)
      && Authentications.isAuthenticatedSubjectAnonymous()
      && configuration.getAnonymousMode() == AnonymousMode.PROTOCOL_ONLY
      && !userAgentParser.parse(request).isBrowser();
  }

  private boolean isMercurialHookRequest(HttpServletRequest request) {
    return request.getRequestURI().startsWith(request.getContextPath() + "/hook/hg/");
  }

  private boolean isLoginRequest(HttpServletRequest request) {
    final String requestURI = request.getRequestURI();
    final String contextPath = request.getContextPath();
    return requestURI != null && requestURI.startsWith(contextPath + "/login");
  }

  private boolean isFullAnonymousAccessEnabled() {
    return configuration.getAnonymousMode() == AnonymousMode.FULL;
  }

  private boolean isHealthCheckRequest(HttpServletRequest request) {
    return request.getRequestURI().startsWith(request.getContextPath() + "/api/" + HealthCheckResource.PATH);
  }
}
