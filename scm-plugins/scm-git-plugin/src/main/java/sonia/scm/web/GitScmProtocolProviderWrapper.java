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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.RootURL;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;

@Singleton
@Extension
public class GitScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {
  @Inject
  public GitScmProtocolProviderWrapper(ScmGitServletProvider servletProvider, RootURL rootURL) {
    super(servletProvider, rootURL);
  }

  @Override
  public String getType() {
    return GitRepositoryHandler.TYPE_NAME;
  }
}
