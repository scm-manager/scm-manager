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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.filter.GZipFilterConfig;
import sonia.scm.filter.GZipResponseWrapper;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.util.WebUtil;

import java.io.IOException;

class SvnGZipFilter implements ScmProviderHttpServlet {

  private static final Logger logger = LoggerFactory.getLogger(SvnGZipFilter.class);

  private final SvnRepositoryHandler handler;
  private final ScmProviderHttpServlet delegate;

  private GZipFilterConfig config = new GZipFilterConfig();

  SvnGZipFilter(SvnRepositoryHandler handler, ScmProviderHttpServlet delegate) {
    this.handler = handler;
    this.delegate = delegate;
    config.setBufferResponse(false);
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException, IOException {
    if (handler.getConfig().isEnabledGZip() && WebUtil.isGzipSupported(request)) {
      logger.trace("compress svn response with gzip");
      GZipResponseWrapper wrappedResponse = new GZipResponseWrapper(response, config);
      delegate.service(request, wrappedResponse, repository);
      wrappedResponse.finishResponse();
    } else {
      logger.trace("skip gzip encoding");
      delegate.service(request, response, repository);
    }
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    delegate.init(config);
  }
}
