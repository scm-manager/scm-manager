package sonia.scm.web.lfs;

import com.github.sdorra.ssp.PermissionCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.Permission;
import sonia.scm.security.Scope;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LfsAccessTokenFactory {

  private static final Logger LOG = LoggerFactory.getLogger(LfsAccessTokenFactory.class);

  private final AccessTokenBuilderFactory tokenBuilderFactory;

  @Inject
  LfsAccessTokenFactory(AccessTokenBuilderFactory tokenBuilderFactory) {
    this.tokenBuilderFactory = tokenBuilderFactory;
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

    return createToken(Scope.valueOf(permissions));
  }

  AccessToken createWriteAccessToken(Repository repository) {
    PermissionCheck read = RepositoryPermissions.read(repository);
    read.check();

    PermissionCheck pull = RepositoryPermissions.pull(repository);
    pull.check();

    PermissionCheck push = RepositoryPermissions.push(repository);
    push.check();

    return createToken(Scope.valueOf(read.asShiroString(), pull.asShiroString(), push.asShiroString()));
  }

  private AccessToken createToken(Scope scope) {
    LOG.trace("create access token with scope: {}", scope);
    return tokenBuilderFactory
      .create()
      .expiresIn(5, TimeUnit.MINUTES)
      .scope(scope)
      .build();
  }
}
