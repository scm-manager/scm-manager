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

import com.google.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletProvider;

public class ScmGitServletProvider extends ScmProviderHttpServletProvider {

  @Inject
  private Provider<ScmGitServlet> servletProvider;

  public ScmGitServletProvider() {
    super(GitRepositoryHandler.TYPE_NAME);
  }

  @Override
  protected ScmProviderHttpServlet getRootServlet() {
    return servletProvider.get();
  }
}
