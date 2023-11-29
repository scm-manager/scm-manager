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
