package sonia.scm.web.lfs;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.Scope;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class LfsAccessTokenFactory {

  private final AccessTokenBuilderFactory tokenBuilderFactory;

  @Inject
  LfsAccessTokenFactory(AccessTokenBuilderFactory tokenBuilderFactory) {
    this.tokenBuilderFactory = tokenBuilderFactory;
  }

  AccessToken getReadAccessToken(Repository repository) {
    return createToken(
      Scope.valueOf(
        RepositoryPermissions.read(repository).asShiroString(),
        RepositoryPermissions.pull(repository).asShiroString()));
  }

  AccessToken getWriteAccessToken(Repository repository) {
    return createToken(
      Scope.valueOf(
        RepositoryPermissions.read(repository).asShiroString(),
        RepositoryPermissions.pull(repository).asShiroString(),
        RepositoryPermissions.push(repository).asShiroString()));
  }

  private AccessToken createToken(Scope scope) {
    return tokenBuilderFactory
      .create()
      .expiresIn(5, TimeUnit.MINUTES)
      .scope(scope)
      .build();
  }
}
