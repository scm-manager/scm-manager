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

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.web.filter.PermissionFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Permission filter for mercurial repositories.
 *
 * @author Sebastian Sdorra
 */
public class HgPermissionFilter extends PermissionFilter {

  private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

  private final HgRepositoryHandler repositoryHandler;

  public HgPermissionFilter(ScmConfiguration configuration, ScmProviderHttpServlet delegate, HgRepositoryHandler repositoryHandler) {
    super(configuration, delegate);
    this.repositoryHandler = repositoryHandler;
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response, Repository repository) throws IOException, ServletException {
    super.service(wrapRequestIfRequired(request), response, repository);
  }

  @VisibleForTesting
  HttpServletRequest wrapRequestIfRequired(HttpServletRequest request) {
    if (isHttpPostArgsEnabled()) {
      return new HgServletRequest(request);
    }
    return request;
  }

  @Override
  public boolean isWriteRequest(HttpServletRequest request) {
    if (isHttpPostArgsEnabled()) {
      return true;
    }
    return isDefaultWriteRequest(request);
  }

  private boolean isHttpPostArgsEnabled() {
    return repositoryHandler.getConfig().isEnableHttpPostArgs();
  }

  private boolean isDefaultWriteRequest(HttpServletRequest request) {
    return !READ_METHODS.contains(request.getMethod());
  }
}
