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
      response.setHeader("X-Frame-Options", "deny");
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
