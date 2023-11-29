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

package sonia.scm.web.lfs;

import com.github.sdorra.ssp.PermissionCheck;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LfsAccessTokenFactory {

  private static final Logger LOG = LoggerFactory.getLogger(LfsAccessTokenFactory.class);

  private final AccessTokenBuilderFactory tokenBuilderFactory;
  private final GitRepositoryHandler handler;

  @Inject
  LfsAccessTokenFactory(AccessTokenBuilderFactory tokenBuilderFactory, GitRepositoryHandler handler) {
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.handler = handler;
  }

  AccessToken createReadAccessToken(Repository repository) {
    PermissionCheck read = RepositoryPermissions.read(repository);
    read.check();

    PermissionCheck pull = RepositoryPermissions.pull(repository);
    pull.check();

    List<String> permissions = new ArrayList<>();
    permissions.add(read.asShiroString());
    permissions.add(pull.asShiroString());

    PermissionCheck push = RepositoryPermissions.push(repository);
    if (push.isPermitted()) {
      // we have to add push permissions,
      // because this token is also used to obtain the write access token
      permissions.add(push.asShiroString());
    }

    return createToken(Scope.valueOf(permissions), 5);
  }

  AccessToken createWriteAccessToken(Repository repository) {
    PermissionCheck read = RepositoryPermissions.read(repository);
    read.check();

    PermissionCheck pull = RepositoryPermissions.pull(repository);
    pull.check();

    PermissionCheck push = RepositoryPermissions.push(repository);
    push.check();

    int lfsAuthorizationTimeoutInMinutes = getConfiguredLfsAuthorizationTimeoutInMinutes();

    return createToken(Scope.valueOf(read.asShiroString(), pull.asShiroString(), push.asShiroString()), lfsAuthorizationTimeoutInMinutes);
  }

  private AccessToken createToken(Scope scope, int expiration) {
    LOG.trace("create access token with scope: {}", scope);
    return tokenBuilderFactory
      .create()
      .expiresIn(expiration, TimeUnit.MINUTES)
      .scope(scope)
      .build();
  }

  private int getConfiguredLfsAuthorizationTimeoutInMinutes() {
    GitConfig repositoryConfig = handler.getConfig();
    return repositoryConfig.getLfsWriteAuthorizationExpirationInMinutes();
  }
}
