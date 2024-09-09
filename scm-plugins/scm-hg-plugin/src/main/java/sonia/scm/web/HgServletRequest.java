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
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;

/**
 * {@link HttpServletRequestWrapper} which adds some functionality in order to support the mercurial client.
 */
public final class HgServletRequest extends HttpServletRequestWrapper {

  private HgServletInputStream hgServletInputStream;

  /**
   * Constructs a request object wrapping the given request.
   *
   * @param request
   * @throws IllegalArgumentException if the request is null
   */
  public HgServletRequest(HttpServletRequest request) {
    super(request);
  }

  @Override
  public HgServletInputStream getInputStream() throws IOException {
    if (hgServletInputStream == null) {
      hgServletInputStream = new HgServletInputStream(super.getInputStream());
    }
    return hgServletInputStream;
  }
}
