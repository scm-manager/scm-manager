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

package sonia.scm.metrics;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;

public final class RequestCategoryDetector {

  private final UserAgentParser userAgentParser;

  @Inject
  public RequestCategoryDetector(UserAgentParser userAgentParser) {
    this.userAgentParser = userAgentParser;
  }

  public RequestCategory detect(HttpServletRequest request) {
    String uri = HttpUtil.getStrippedURI(request);
    if (isStatic(uri)) {
      return RequestCategory.STATIC;
    } else if (HttpUtil.isWUIRequest(request)) {
      return RequestCategory.UI;
    } else if (uri.startsWith("/api/")) {
      return RequestCategory.API;
    } else if (uri.startsWith("/repo/") && isScmClient(request)) {
      return RequestCategory.PROTOCOL;
    }
    return RequestCategory.UNKNOWN;
  }

  private boolean isStatic(String uri) {
    return uri.startsWith("/assets")
      || uri.endsWith(".js")
      || uri.endsWith(".css")
      || uri.endsWith(".jpg")
      || uri.endsWith(".jpeg")
      || uri.endsWith(".png")
      || uri.endsWith(".gif")
      || uri.endsWith(".svg")
      || uri.endsWith(".html");
  }

  private boolean isScmClient(HttpServletRequest request) {
    UserAgent agent = userAgentParser.parse(request);
    return agent != null && agent.isScmClient();
  }

}
