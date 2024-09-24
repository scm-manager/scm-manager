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
