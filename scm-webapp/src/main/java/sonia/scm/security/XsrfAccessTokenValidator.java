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

import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.plugin.Extension;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Validates xsrf protected access tokens. The validator check if the current request contains an xsrf key which is
 * equal to the one in the access token. If the token does not contain a xsrf key, the check is passed by. The xsrf keys
 * are added by the {@link XsrfAccessTokenEnricher}.
 * 
 * @since 2.0.0
 */
@Extension
public class XsrfAccessTokenValidator implements AccessTokenValidator {

  private static final Set<String> ALLOWED_METHOD = ImmutableSet.of(
    "GET", "HEAD", "OPTIONS"
  );

  private final Provider<HttpServletRequest> requestProvider;
  private final XsrfExcludes excludes;
  
  @Inject
  public XsrfAccessTokenValidator(Provider<HttpServletRequest> requestProvider, XsrfExcludes excludes) {
    this.requestProvider = requestProvider;
    this.excludes = excludes;
  }
  
  @Override
  public boolean validate(AccessToken accessToken) {
    Optional<String> xsrfClaim = accessToken.getCustom(Xsrf.TOKEN_KEY);
    if (xsrfClaim.isPresent()) {
      HttpServletRequest request = requestProvider.get();

      if (excludes.contains(request.getRequestURI())) {
        return true;
      }

      String xsrfHeaderValue = request.getHeader(Xsrf.HEADER_KEY);
      return ALLOWED_METHOD.contains(request.getMethod().toUpperCase(Locale.ENGLISH))
        || xsrfClaim.get().equals(xsrfHeaderValue);
    }
    return true;
  }
  
}
