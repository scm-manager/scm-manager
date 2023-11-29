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
