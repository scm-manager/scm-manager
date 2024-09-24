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

package sonia.scm.repository;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.plugin.ExtensionPoint;

import java.io.IOException;

/**
 * Listener before a repository request is executed. Repository request are
 * request to a repository from a client like git, mercurial or svn.
 * 
 * TODO replace with event bus implementation.
 *
 * @since 1.10
 */
@ExtensionPoint
public interface RepositoryRequestListener
{

  /**
   * Handle repository requests. Return false to abort the request.
   *
   *
   * @param request the servletrequest
   * @param response the servletresponse
   * @param repository the requested repository
   *
   *
   * @return false to abort the request
   * @throws IOException
   */
  boolean handleRequest(HttpServletRequest request, HttpServletResponse response, Repository repository) throws IOException;
}
