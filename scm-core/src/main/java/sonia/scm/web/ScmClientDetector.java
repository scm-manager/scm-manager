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
import sonia.scm.plugin.ExtensionPoint;

/**
 * This can be used to determine, whether a web request should be handled as a scm client request.
 *
 * @since 2.26.0
 */
@ExtensionPoint
public interface ScmClientDetector {

  /**
   * Checks whether the given request and/or the userAgent imply a request from a scm client.
   *
   * @param request The request to check.
   * @param userAgent The {@link UserAgent} for the request.
   * @return <code>true</code> if the given request was sent by an scm client.
   */
  boolean isScmClient(HttpServletRequest request, UserAgent userAgent);
}
