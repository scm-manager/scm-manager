/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.repository;

import sonia.scm.plugin.Extension;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

/**
 * Matches git repositories with ".git" and without ".git".
 *
 * @author Sebastian Sdorra
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
