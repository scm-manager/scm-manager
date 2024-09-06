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

package sonia.scm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The PushStateDispatcher is responsible for dispatching the request, to the main entry point of the ui, if no resource
 * could be found for the requested path. This allows us the implementation of a ui which work with "pushstate" of
 * html5.
 *
 * @since 2.0.0
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">HTML5 Push State</a>
 */
public interface PushStateDispatcher {

  /**
   * Dispatches the request to the main entry point of the ui.
   *
   * @param request http request
   * @param response http response
   * @param uri request uri
   *
   * @throws IOException
   */
  void dispatch(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException;

}
