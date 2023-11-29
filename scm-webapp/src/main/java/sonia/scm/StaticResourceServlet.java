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
