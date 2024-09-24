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
