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

import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.Priority;
import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;
import sonia.scm.web.filter.HttpFilter;

import java.io.IOException;

@Priority(7000)
@WebElement("*")
public class SecurityHeadersFilter extends HttpFilter {

  private final SCMContextProvider contextProvider;

  @Inject
  public SecurityHeadersFilter(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (contextProvider.getStage() != Stage.TESTING) {
      response.setHeader("X-Frame-Options", "sameorigin");
      response.setHeader("X-Content-Type-Options", "nosniff");
      response.setHeader("Content-Security-Policy",
        "form-action 'self'; " +
          "object-src 'self'; " +
          "frame-ancestors 'self'; " +
          "block-all-mixed-content"
      );
      response.setHeader("Permissions-Policy",
        "accelerometer=()," +
          "ambient-light-sensor=()," +
          "autoplay=()," +
          "battery=()," +
          "camera=()," +
          "display-capture=()," +
          "document-domain=()," +
          "encrypted-media=()," +
          "fullscreen=()," +
          "gamepad=()," +
          "geolocation=()," +
          "gyroscope=()," +
          "layout-animations=(self)," +
          "legacy-image-formats=(self)," +
          "magnetometer=()," +
          "microphone=()," +
          "midi=()," +
          "oversized-images=(self)," +
          "payment=()," +
          "picture-in-picture=()," +
          "publickey-credentials-get=()," +
          "speaker-selection=()," +
          "sync-xhr=(self)," +
          "unoptimized-images=(self)," +
          "unsized-media=(self)," +
          "usb=()," +
          "screen-wake-lock=()," +
          "web-share=()," +
          "xr-spatial-tracking=()"
      );
    }
    chain.doFilter(request, response);
  }
}
