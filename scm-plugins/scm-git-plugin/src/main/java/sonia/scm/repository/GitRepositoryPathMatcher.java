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
