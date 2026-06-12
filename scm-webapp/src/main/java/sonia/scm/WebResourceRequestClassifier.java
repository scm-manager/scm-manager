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

import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.util.HttpUtil;

/**
 * Classifies requests for public static web resources.
 */
public final class WebResourceRequestClassifier {

  private WebResourceRequestClassifier() {
  }

  public static boolean isPublicStaticResource(HttpServletRequest request) {
    return isPublicStaticResource(HttpUtil.getStrippedURI(request));
  }

  public static boolean isPublicStaticResource(String uri) {
    return uri.startsWith("/assets/")
      || uri.startsWith("/images/")
      || uri.startsWith("/locales/")
      || uri.equals("/favicon.ico")
      || uri.equals("/manifest.json");
  }
}
