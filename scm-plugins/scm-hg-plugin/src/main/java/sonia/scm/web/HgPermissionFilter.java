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

import com.google.common.annotations.VisibleForTesting;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.web.filter.PermissionFilter;

import java.io.IOException;

/**
 * Permission filter for mercurial repositories.
 *
 */
public class HgPermissionFilter extends PermissionFilter {

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
    // The repository permissions for read and write are handled by hg internally.
    // Therefore, we ignore the checks here.
    return false;
  }

  private boolean isHttpPostArgsEnabled() {
    return repositoryHandler.getConfig().isEnableHttpPostArgs();
  }
}
