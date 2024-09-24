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


import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import sonia.scm.plugin.ExtensionPoint;

/**
 * Creates an {@link AuthenticationToken} from a {@link HttpServletRequest}.
 *
 * @since 2.0.0
 */
@ExtensionPoint
public interface WebTokenGenerator
{

  /**
   * Returns an {@link AuthenticationToken} or {@code null}.
   *
   * @param request http servlet request
   */
  public AuthenticationToken createToken(HttpServletRequest request);
}
