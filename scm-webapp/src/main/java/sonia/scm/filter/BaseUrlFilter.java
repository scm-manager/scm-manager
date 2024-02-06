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

package sonia.scm.filter;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;
import sonia.scm.web.filter.HttpFilter;

import java.io.IOException;


@Priority(Filters.PRIORITY_BASEURL)
@WebElement(Filters.PATTERN_ALL)
public class BaseUrlFilter extends HttpFilter {

  private final ScmConfiguration configuration;

  @Inject
  public BaseUrlFilter(ScmConfiguration configuration) {
    this.configuration = configuration;
  }

  @VisibleForTesting
  boolean startsWith(String requestUrl, String baseUrl) {
    return HttpUtil.normalizeUrl(requestUrl).startsWith(HttpUtil.normalizeUrl(baseUrl));
  }

  @Override
  protected void doFilter(
    HttpServletRequest request, HttpServletResponse response, FilterChain chain
  ) throws IOException, ServletException {
    if (Util.isEmpty(configuration.getBaseUrl())) {
      configuration.setBaseUrl(createDefaultBaseUrl(request));
    }

    if (!configuration.isForceBaseUrl() || isBaseUrl(request)) {
      chain.doFilter(request, response);
    } else {
      String url = HttpUtil.getCompleteUrl(configuration, HttpUtil.getStrippedURI(request));
      response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
      response.setHeader(HttpUtil.HEADER_LOCATION, url);
    }
  }

  private String createDefaultBaseUrl(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder(request.getScheme());

    sb.append("://").append(request.getServerName()).append(":");
    sb.append(request.getServerPort());
    sb.append(request.getContextPath());

    return sb.toString();
  }

  private boolean isBaseUrl(HttpServletRequest request) {
    return startsWith(request.getRequestURL().toString(), configuration.getBaseUrl());
  }
}
