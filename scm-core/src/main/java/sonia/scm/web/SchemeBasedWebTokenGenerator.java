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

package sonia.scm.web;


import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @since 2.0.0
 */
public abstract class SchemeBasedWebTokenGenerator implements WebTokenGenerator {

  /** authorization header */
  private static final String HEADER_AUTHORIZATION = "Authorization";

  private static final Logger LOG = LoggerFactory.getLogger(SchemeBasedWebTokenGenerator.class);

  protected abstract AuthenticationToken createToken(HttpServletRequest request, String scheme, String authorization);

  @Override
  public AuthenticationToken createToken(HttpServletRequest request) {
    AuthenticationToken token = null;
    String authorization = request.getHeader(HEADER_AUTHORIZATION);

    if (!Strings.isNullOrEmpty(authorization)) {
      String[] parts = authorization.split("\\s+");

      if (parts.length > 0) {
        token = createToken(request, parts[0], parts[1]);

        if (token == null) {
          LOG.debug("could not create token from authentication header");
        }
      } else {
        LOG.warn("found malformed authentication header");
      }
    }

    return token;
  }
}
