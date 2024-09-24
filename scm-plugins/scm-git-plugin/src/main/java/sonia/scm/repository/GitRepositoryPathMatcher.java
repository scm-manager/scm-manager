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

package sonia.scm.repository;

import sonia.scm.plugin.Extension;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

/**
 * Matches git repositories with ".git" and without ".git".
 *
 * @since 1.54
 */
@Extension
public class GitRepositoryPathMatcher implements RepositoryPathMatcher {

  @Override
  public boolean isPathMatching(Repository repository, String path) {
    String repositoryName = repository.getName();

    if (path.startsWith(repositoryName)) {

      String pathPart = path.substring(repositoryName.length());

      // git repository may also be named <<repo-name>>.git by convention
      if (pathPart.startsWith(GitRepositoryHandler.DOT_GIT)) {
        // if this is the case, just also cut it away
        pathPart = pathPart.substring(GitRepositoryHandler.DOT_GIT.length());
      }

      return Util.isEmpty(pathPart) || pathPart.startsWith(HttpUtil.SEPARATOR_PATH);
    }

    return false;
  }

  @Override
  public String getType() {
    return GitRepositoryHandler.TYPE_NAME;
  }

}
