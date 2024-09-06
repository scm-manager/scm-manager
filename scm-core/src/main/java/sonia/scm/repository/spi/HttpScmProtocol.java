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

package sonia.scm.repository.spi;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.net.URI;

public abstract class HttpScmProtocol implements ScmProtocol {

  private final Repository repository;
  private final String basePath;

  public HttpScmProtocol(Repository repository, String basePath) {
    this.repository = repository;
    this.basePath = basePath;
  }

  @Override
  public String getType() {
    return "http";
  }

  @Override
  public String getUrl() {
    return URI.create(basePath + "/").resolve(String.format("repo/%s/%s", HttpUtil.encode(repository.getNamespace()), HttpUtil.encode(repository.getName()))).toASCIIString();
  }

  @Override
  public int getPriority() {
    return 150;
  }

  public final void serve(HttpServletRequest request, HttpServletResponse response, ServletConfig config) throws ServletException, IOException {
    serve(request, response, repository, config);
  }

  protected abstract void serve(HttpServletRequest request, HttpServletResponse response, Repository repository, ServletConfig config) throws ServletException, IOException;
}
