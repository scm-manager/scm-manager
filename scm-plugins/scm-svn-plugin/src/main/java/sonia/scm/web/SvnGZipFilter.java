/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
