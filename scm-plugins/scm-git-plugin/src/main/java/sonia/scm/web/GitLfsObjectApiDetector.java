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

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.plugin.Extension;

import java.util.regex.Pattern;

@Slf4j
@Extension
public class GitLfsObjectApiDetector implements ScmClientDetector {

  private static final Pattern OBJECT_PATH_PATTERN = Pattern.compile("/[^/]*/repo/[^/]*/[^/]*\\.git/info/lfs/objects/.*");

  @Override
  public boolean isScmClient(HttpServletRequest request, UserAgent userAgent) {
    return OBJECT_PATH_PATTERN.matcher(request.getRequestURI()).matches();
  }
}
