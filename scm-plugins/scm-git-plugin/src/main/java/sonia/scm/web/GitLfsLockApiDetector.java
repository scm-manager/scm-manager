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
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.plugin.Extension;

import java.util.Arrays;

@Slf4j
@Extension
public class GitLfsLockApiDetector implements ScmClientDetector {

  private static final String APPLICATION_TYPE = "application";
  private static final String LFS_VND_SUB_TYPE = "vnd.git-lfs+json";

  @Override
  public boolean isScmClient(HttpServletRequest request, UserAgent userAgent) {
    return isLfsType(request, "Content-Type")
      || isLfsType(request, "Accept");
  }

  private boolean isLfsType(HttpServletRequest request, String name) {
    String headerValue = request.getHeader(name);

    if (headerValue == null) {
      return false;
    }

    log.trace("checking '{}' header with value '{}'", name, headerValue);

    return Arrays.stream(headerValue.split(",\\s*"))
      .anyMatch(v -> {
        MediaType headerMediaType = MediaType.valueOf(v);
        return APPLICATION_TYPE.equals(headerMediaType.getType())
          && LFS_VND_SUB_TYPE.equals(headerMediaType.getSubtype());
      });
  }
}
