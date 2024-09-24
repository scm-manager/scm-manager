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

import com.github.sdorra.webresources.CacheControl;
import com.github.sdorra.webresources.WebResourceSender;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Serves static resources from servlet context.
 */
@Singleton
public class StaticResourceServlet extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(StaticResourceServlet.class);

  private final WebResourceSender sender = WebResourceSender.create()
    .withGZIP()
    .withGZIPMinLength(512)
    .withBufferSize(16384)
    .withCacheControl(CacheControl.create().noCache());

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      URL resource = createResourceUrlFromRequest(request);
      if (resource != null) {
        sender.resource(resource).get(request, response);
      } else {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (IOException ex) {
      LOG.warn("failed to serve resource", ex);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private URL createResourceUrlFromRequest(HttpServletRequest request) throws MalformedURLException {
    String uri = HttpUtil.getStrippedURI(request);
    return request.getServletContext().getResource(uri);
  }
}
